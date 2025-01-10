package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.common.AbsenceType;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.StepName;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.domain.model.monthlyreport.JourneyWarning;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntryWarning;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.personio.employees.PersonioEmployeesService;
import com.gepardec.mega.rest.model.MappedTimeWarningDTO;
import com.gepardec.mega.rest.model.PmProgressDto;
import com.gepardec.mega.service.api.CommentService;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.MonthlyReportService;
import com.gepardec.mega.service.api.PrematureEmployeeCheckService;
import com.gepardec.mega.service.api.StepEntryService;
import com.gepardec.mega.service.helper.WarningCalculatorsManager;
import com.gepardec.mega.service.helper.WorkingTimeUtil;
import com.gepardec.mega.service.mapper.TimeWarningMapper;
import com.gepardec.mega.zep.ZepService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gepardec.mega.domain.utils.DateUtils.getFirstDayOfMonth;
import static com.gepardec.mega.domain.utils.DateUtils.getLastDayOfMonth;

@RequestScoped
public class MonthlyReportServiceImpl implements MonthlyReportService {

    @Inject
    ZepService zepService;

    @Inject
    WarningCalculatorsManager warningCalculatorsManager;

    @Inject
    CommentService commentService;

    @Inject
    StepEntryService stepEntryService;

    @Inject
    TimeWarningMapper timeWarningMapper;

    @Inject
    UserContext userContext;

    @Inject
    EmployeeService employeeService;

    @Inject
    PersonioEmployeesService personioEmployeesService;

    @Inject
    WorkingTimeUtil workingTimeUtil;

    @Inject
    PrematureEmployeeCheckService prematureEmployeeCheckService;

    @Override
    public MonthlyReport getMonthEndReportForUser() {
        Employee employee = employeeService.getEmployee(userContext.getUser().getUserId());

        LocalDate initialDate = getCorrectInitialDateForMonthEndReport(employee);

        return getMonthEndReportForUser(initialDate.getYear(), initialDate.getMonthValue(), employee, initialDate);
    }

    private LocalDate getCorrectInitialDateForMonthEndReport(Employee employee) {
        LocalDate midOfCurrentMonth = LocalDate.now().withDayOfMonth(14);
        LocalDate now = LocalDate.now();
        LocalDate firstOfPreviousMonth = now.minusMonths(1).withDayOfMonth(1);

        if (now.isAfter(midOfCurrentMonth) && isMonthConfirmedFromEmployee(employee, firstOfPreviousMonth)) {
            return now;
        } else {
            return firstOfPreviousMonth;
        }
    }

    public boolean isMonthConfirmedFromEmployee(Employee employee, LocalDate date) {
        return stepEntryService.findControlTimesStepEntry(employee, date).stream()
                .allMatch(stepEnry -> stepEnry.getState().equals(EmployeeState.DONE));
    }

    @Override
    public MonthlyReport getMonthEndReportForUser(Integer year, Integer month, Employee employee, LocalDate initialDate) {
        LocalDate date = DateUtils.getFirstDayOfMonth(year, month);

        if (employee == null) {
            employee = employeeService.getEmployee(userContext.getUser().getUserId());
        }


        return buildMonthlyReport(
                employee,
                date,
                zepService.getProjectTimes(employee, date),
                zepService.getBillableForEmployee(employee, date),
                zepService.getAbsenceForEmployee(employee, date),
                stepEntryService.findEmployeeCheckState(employee, date),
                stepEntryService.findEmployeeInternalCheckState(employee, date),
                initialDate
        );
    }

    @Override
    public boolean isMonthCompletedForEmployee(Employee employee, LocalDate date) {
        List<StepEntry> allOwnedAndUnassignedStepEntriesForOtherChecks = stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(employee, date);

        Map<String, List<StepEntry>> controlTimeEvidencesStepsByProjects = allOwnedAndUnassignedStepEntriesForOtherChecks.stream()
                .filter(se -> StepName.CONTROL_TIME_EVIDENCES.name().equals(se.getStep().getName()))
                .collect(Collectors.groupingBy(StepEntry::getProject));

        // mind. 1 Projektleiter muss Employee auf Done gesetzt haben, bei Step 4 (Control Time Evidences)
        boolean controlTimeEvidencesDone = controlTimeEvidencesStepsByProjects.entrySet().stream()
                .allMatch(this::isAnyStepEntryDone);

        return controlTimeEvidencesDone &&
                allOwnedAndUnassignedStepEntriesForOtherChecks.stream()
                        .filter(se -> !StepName.CONTROL_TIME_EVIDENCES.name().equals(se.getStep().getName()))
                        .allMatch(this::isStepEntryDone);
    }

    private MonthlyReport buildMonthlyReport(
            Employee employee,
            LocalDate date,
            List<ProjectEntry> projectEntries,
            List<ProjectTime> billableEntries,
            List<AbsenceTime> absenceEntries,
            Optional<Pair<EmployeeState, String>> employeeCheckState,
            Optional<EmployeeState> internalCheckState,
            LocalDate initialDate
    ) {
        final List<JourneyWarning> journeyWarnings = warningCalculatorsManager.determineJourneyWarnings(projectEntries);
        final List<TimeWarning> timeWarnings = warningCalculatorsManager.determineTimeWarnings(projectEntries);
        timeWarnings.addAll(warningCalculatorsManager.determineNoTimeEntries(employee, projectEntries, absenceEntries));
        timeWarnings.sort(Comparator.comparing(ProjectEntryWarning::getDate));

        int year = date.getYear();
        int month = date.getMonthValue();

        List<Comment> comments = commentService.findCommentsForEmployee(employee.getEmail(), getFirstDayOfMonth(year, month), getLastDayOfMonth(year, month));

        final List<PmProgressDto> pmProgressDtos =
                stepEntryService.findAllOwnedAndUnassignedStepEntriesForPMProgress(employee.getEmail(), date)
                        .stream()
                        .map(PmProgressDto::ofStepEntry)
                        .toList();

        List<MappedTimeWarningDTO> mappedTimeWarnings = timeWarningMapper.map(timeWarnings);
        var prematureEmployeeCheck = prematureEmployeeCheckService.findByEmailAndMonth(employee.getEmail(), date);
        String stepEntryForAutomaticReleaseReason = null;
        try {
            var stepEntry = stepEntryService.findStepEntryForEmployeeAtStep(1L, employee.getEmail(), employee.getEmail(), LocalDate.of(year, month, 1).toString());
            if (stepEntry != null) {
                stepEntryForAutomaticReleaseReason = stepEntry.getStateReason();
            }
        } catch (IllegalStateException exception) {
            // is already null here
        }

        MonthlyReport.Builder builder = MonthlyReport.builder()
                .employee(employee)
                .timeWarnings(mappedTimeWarnings)
                .journeyWarnings(journeyWarnings)
                .comments(comments)
                .employeeCheckState(employeeCheckState.map(Pair::getLeft).orElse(EmployeeState.PREMATURE_CHECK))
                .employeeCheckStateReason(employeeCheckState.map(Pair::getRight).orElse(prematureEmployeeCheck.map(PrematureEmployeeCheck::getReason).orElse(stepEntryForAutomaticReleaseReason)))
                .internalCheckState(internalCheckState.orElse(EmployeeState.OPEN))
                .employeeProgresses(pmProgressDtos)
                .otherChecksDone(isMonthCompletedForEmployee(employee, date))
                .billableTime(workingTimeUtil.getBillableTimesForEmployee(billableEntries, employee))
                .totalWorkingTime(workingTimeUtil.getTotalWorkingTimeForEmployee(projectEntries, employee))
                .compensatoryDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.COMPENSATORY_DAYS.getAbsenceName(), date))
                .homeofficeDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.HOME_OFFICE_DAYS.getAbsenceName(), date))
                .vacationDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.VACATION_DAYS.getAbsenceName(), date))
                .nursingDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.NURSING_DAYS.getAbsenceName(), date))
                .maternityLeaveDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.MATERNITY_LEAVE_DAYS.getAbsenceName(), date))
                .externalTrainingDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.EXTERNAL_TRAINING_DAYS.getAbsenceName(), date))
                .conferenceDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.CONFERENCE_DAYS.getAbsenceName(), date))
                .maternityProtectionDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.MATERNITY_PROTECTION_DAYS.getAbsenceName(), date))
                .fatherMonthDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.FATHER_MONTH_DAYS.getAbsenceName(), date))
                .paidSpecialLeaveDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.PAID_SPECIAL_LEAVE_DAYS.getAbsenceName(), date))
                .nonPaidVacationDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.NON_PAID_VACATION_DAYS.getAbsenceName(), date))
                .paidSickLeave(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.PAID_SICK_LEAVE.getAbsenceName(), date))
                .overtime(workingTimeUtil.getOvertimeForEmployee(employee, billableEntries, absenceEntries, date))
                .prematureEmployeeCheck(prematureEmployeeCheck.orElse(null))
                .initialDate(initialDate);

        return addPersonioEmployee(builder, employee.getEmail()).build();
    }

    private boolean isAnyStepEntryDone(Map.Entry<String, List<StepEntry>> entry) {
        return entry.getValue().stream().anyMatch(this::isStepEntryDone);
    }

    private boolean isStepEntryDone(StepEntry stepEntry) {
        return stepEntry.getState() == EmployeeState.DONE;
    }

    private MonthlyReport.Builder addPersonioEmployee(MonthlyReport.Builder builder, String email) {
        personioEmployeesService.getPersonioEmployeeByEmail(email).ifPresent(
                employee -> {
                    builder.internalProjectLead(employee.getInternalProjectLead());
                    builder.guildLead(employee.getGuildLead());
                    builder.vacationDayBalance(employee.getVacationDayBalance());
                }
        );

        return builder;
    }

}
