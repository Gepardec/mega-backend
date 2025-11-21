package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.common.AbsenceType;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.EmployeeCheck;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.StepName;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.domain.model.monthlyreport.JourneyWarning;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntryWarning;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
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
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public MonthlyReport getMonthEndReportForUser(YearMonth payrollMonth) {
        Employee employee = employeeService.getEmployee(userContext.getUser().getUserId());
        return buildMonthlyReport(
                employee,
                payrollMonth,
                zepService.getProjectTimes(employee, payrollMonth),
                zepService.getBillableForEmployee(employee, payrollMonth),
                zepService.getAbsenceForEmployee(employee, payrollMonth),
                stepEntryService.findEmployeeCheckState(employee, payrollMonth),
                stepEntryService.findEmployeeInternalCheckState(employee, payrollMonth),
                payrollMonth.atDay(1)
        );
    }

    private boolean isMonthCompletedForEmployee(Employee employee, YearMonth payrollMonth) {
        List<StepEntry> allOwnedAndUnassignedStepEntriesForOtherChecks = stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(employee, payrollMonth);

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

    public EmployeeCheck getEmployeeCheck(YearMonth payrollMonth) {
        var employee = employeeService.getEmployee(userContext.getUser().getUserId());
        var employeeCheckState = stepEntryService.findEmployeeCheckState(employee, payrollMonth);
        var internalCheckState = stepEntryService.findEmployeeInternalCheckState(employee, payrollMonth);

        return buildEmployeeCheck(employee, employeeCheckState, internalCheckState, payrollMonth);
    }

    private EmployeeCheck buildEmployeeCheck(Employee employee, Optional<Pair<EmployeeState, String>> employeeCheckState, Optional<EmployeeState> internalCheckState, YearMonth payrollMonth) {
        List<Comment> comments = commentService.findCommentsForEmployee(employee.getEmail(), payrollMonth);

        var prematureEmployeeCheck = prematureEmployeeCheckService.findByEmailAndMonth(employee.getEmail(), payrollMonth);
        String stepEntryForAutomaticReleaseReason = null;
        try {
            var stepEntry = stepEntryService.findStepEntryForEmployeeAtStep(1L, employee.getEmail(), employee.getEmail(), payrollMonth);
            if (stepEntry != null) {
                stepEntryForAutomaticReleaseReason = stepEntry.getStateReason();
            }
        } catch (IllegalStateException exception) {
            // is already null here
        }

        var employeeCheckState2 = employeeCheckState.map(Pair::getLeft).orElse(EmployeeState.PREMATURE_CHECK);
        if (employeeCheckState2 == EmployeeState.PREMATURE_CHECK && payrollMonth.isBefore(YearMonth.now())) {
            employeeCheckState2 = null;
        }

        return new EmployeeCheck(
                employee,
                employeeCheckState2,
                employeeCheckState.map(Pair::getRight).orElse(prematureEmployeeCheck.map(PrematureEmployeeCheck::getReason).orElse(stepEntryForAutomaticReleaseReason)),
                internalCheckState.orElse(null),
                isMonthCompletedForEmployee(employee, payrollMonth),
                comments,
                prematureEmployeeCheck.orElse(null)
        );
    }

    private MonthlyReport buildMonthlyReport(
            Employee employee,
            YearMonth payrollMonth,
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

        List<Comment> comments = commentService.findCommentsForEmployee(employee.getEmail(), payrollMonth);

        final List<PmProgressDto> pmProgressDtos =
                stepEntryService.findAllOwnedAndUnassignedStepEntriesForPMProgress(employee.getEmail(), payrollMonth)
                        .stream()
                        .map(PmProgressDto::ofStepEntry)
                        .toList();

        List<MappedTimeWarningDTO> mappedTimeWarnings = timeWarningMapper.map(timeWarnings);
        var prematureEmployeeCheck = prematureEmployeeCheckService.findByEmailAndMonth(employee.getEmail(), payrollMonth);
        String stepEntryForAutomaticReleaseReason = null;
        try {
            var stepEntry = stepEntryService.findStepEntryForEmployeeAtStep(1L, employee.getEmail(), employee.getEmail(), payrollMonth);
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
                .otherChecksDone(isMonthCompletedForEmployee(employee, payrollMonth))
                .billableTime(workingTimeUtil.getBillableTimesForEmployee(billableEntries, employee))
                .totalWorkingTime(workingTimeUtil.getTotalWorkingTimeForEmployee(projectEntries))
                .compensatoryDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.COMPENSATORY_DAYS.getAbsenceName(), payrollMonth))
                .homeofficeDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.HOME_OFFICE_DAYS.getAbsenceName(), payrollMonth))
                .vacationDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.VACATION_DAYS.getAbsenceName(), payrollMonth))
                .nursingDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.NURSING_DAYS.getAbsenceName(), payrollMonth))
                .maternityLeaveDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.MATERNITY_LEAVE_DAYS.getAbsenceName(), payrollMonth))
                .externalTrainingDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.EXTERNAL_TRAINING_DAYS.getAbsenceName(), payrollMonth))
                .conferenceDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.CONFERENCE_DAYS.getAbsenceName(), payrollMonth))
                .maternityProtectionDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.MATERNITY_PROTECTION_DAYS.getAbsenceName(), payrollMonth))
                .fatherMonthDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.FATHER_MONTH_DAYS.getAbsenceName(), payrollMonth))
                .paidSpecialLeaveDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.PAID_SPECIAL_LEAVE_DAYS.getAbsenceName(), payrollMonth))
                .nonPaidVacationDays(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.NON_PAID_VACATION_DAYS.getAbsenceName(), payrollMonth))
                .paidSickLeave(workingTimeUtil.getAbsenceTimesForEmployee(absenceEntries, AbsenceType.PAID_SICK_LEAVE.getAbsenceName(), payrollMonth))
                .overtime(workingTimeUtil.getOvertimeForEmployee(employee, projectEntries, absenceEntries, payrollMonth))
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
