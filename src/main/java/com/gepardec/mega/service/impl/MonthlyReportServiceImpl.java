package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.domain.model.Employee;
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
import com.gepardec.mega.service.api.StepEntryService;
import com.gepardec.mega.service.helper.WarningCalculatorsManager;
import com.gepardec.mega.service.helper.WorkingTimeFilterHelper;
import com.gepardec.mega.service.mapper.TimeWarningMapper;
import com.gepardec.mega.zep.ZepService;
import de.provantis.zep.FehlzeitType;
import de.provantis.zep.ProjektzeitType;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gepardec.mega.domain.utils.DateUtils.getFirstDayOfMonth;
import static com.gepardec.mega.domain.utils.DateUtils.getLastDayOfMonth;

@RequestScoped
public class MonthlyReportServiceImpl implements MonthlyReportService {

    public static final String COMPENSATORY_DAYS = "FA";
    public static final String HOME_OFFICE_DAYS = "HO";
    public static final String VACATION_DAYS = "UB";
    public static final String NURSING_DAYS = "PU";
    public static final String MATERNITY_LEAVE_DAYS = "KA";
    public static final String EXTERNAL_TRAINING_DAYS = "EW";
    public static final String CONFERENCE_DAYS = "KO";
    public static final String MATERNITY_PROTECTION_DAYS = "MU";
    public static final String FATHER_MONTH_DAYS = "PA";
    public static final String PAID_SPECIAL_LEAVE_DAYS = "SU";
    public static final String NON_PAID_VACATION_DAYS = "UU";
    public static final String PAID_SICK_LEAVE = "KR";

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
    WorkingTimeFilterHelper workingTimeFilterHelper;

    @Override
    public MonthlyReport getMonthEndReportForUser() {
        Employee employee = employeeService.getEmployee(userContext.getUser().getUserId());

        LocalDate initialDate = getCorrectInitialDateForMonthEndReport(employee);

        MonthlyReport monthlyReport = getMonthEndReportForUser(initialDate.getYear(), initialDate.getMonthValue(), employee);
        monthlyReport.setInitialDate(initialDate);

        return monthlyReport;
    }

    private LocalDate getCorrectInitialDateForMonthEndReport(Employee employee) {
        LocalDate midOfCurrentMonth = LocalDate.now().withDayOfMonth(14);
        LocalDate now = LocalDate.now();
        LocalDate firstOfPreviousMonth = now.withMonth(now.getMonth().minus(1).getValue()).withDayOfMonth(1);

        if (now.isAfter(midOfCurrentMonth) && isMonthConfirmedFromEmployee(employee, firstOfPreviousMonth)) {
            return now;
        } else {
            return firstOfPreviousMonth;
        }
    }

    private boolean isMonthConfirmedFromEmployee(Employee employee, LocalDate date) {
        return stepEntryService.findControlTimesStepEntry(employee, date).stream()
                .allMatch(stepEnry -> stepEnry.getState().equals(EmployeeState.DONE));
    }

    @Override
    public MonthlyReport getMonthEndReportForUser(Integer year, Integer month, Employee employee) {
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
                stepEntryService.findEmployeeInternalCheckState(employee, date)
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
            List<ProjektzeitType> billableEntries,
            List<FehlzeitType> absenceEntries,
            Optional<Pair<EmployeeState, String>> employeeCheckState,
            Optional<EmployeeState> internalCheckState
    ) {
        final List<JourneyWarning> journeyWarnings = warningCalculatorsManager.determineJourneyWarnings(projectEntries);
        final List<TimeWarning> timeWarnings = warningCalculatorsManager.determineTimeWarnings(projectEntries);
        timeWarnings.addAll(warningCalculatorsManager.determineNoTimeEntries(employee, projectEntries, absenceEntries));
        timeWarnings.sort(Comparator.comparing(ProjectEntryWarning::getDate));

        int year = date.getYear();
        int month = date.getMonthValue();

        List<Comment> comments = commentService.findCommentsForEmployee(employee, getFirstDayOfMonth(year, month), getLastDayOfMonth(year, month));

        final List<PmProgressDto> pmProgressDtos = Optional.ofNullable(employee)
                .map(empl -> stepEntryService.findAllOwnedAndUnassignedStepEntriesForPMProgress(empl.getEmail(), date))
                .orElse(Collections.emptyList())
                .stream()
                .map(PmProgressDto::ofStepEntry)
                .collect(Collectors.toList());

        List<MappedTimeWarningDTO> mappedTimeWarnings = timeWarningMapper.map(timeWarnings);

        billableEntries.forEach(projektzeitType -> System.out.println(projektzeitType.getDauer()));

        return MonthlyReport.builder()
                .employee(employee)
                .timeWarnings(mappedTimeWarnings)
                .journeyWarnings(journeyWarnings)
                .comments(comments)
                .employeeCheckState(employeeCheckState.map(Pair::getLeft).orElse(EmployeeState.OPEN))
                .employeeCheckStateReason(employeeCheckState.map(Pair::getRight).orElse(null))
                .internalCheckState(internalCheckState.orElse(EmployeeState.OPEN))
                .isAssigned(employeeCheckState.isPresent())
                .employeeProgresses(pmProgressDtos)
                .otherChecksDone(isMonthCompletedForEmployee(employee, date))
                .billableTime(workingTimeFilterHelper.getBillableTimesForEmployee(billableEntries, employee))
                .totalWorkingTime(workingTimeFilterHelper.getTotalWorkingTimeForEmployee(billableEntries, employee))
                .compensatoryDays(workingTimeFilterHelper.getAbsenceTimesForEmployee(absenceEntries, COMPENSATORY_DAYS, date))
                .homeofficeDays(workingTimeFilterHelper.getAbsenceTimesForEmployee(absenceEntries, HOME_OFFICE_DAYS, date))
                .vacationDays(workingTimeFilterHelper.getAbsenceTimesForEmployee(absenceEntries, VACATION_DAYS, date))
                .nursingDays(workingTimeFilterHelper.getAbsenceTimesForEmployee(absenceEntries, NURSING_DAYS, date))
                .maternityLeaveDays(workingTimeFilterHelper.getAbsenceTimesForEmployee(absenceEntries, MATERNITY_LEAVE_DAYS, date))
                .externalTrainingDays(workingTimeFilterHelper.getAbsenceTimesForEmployee(absenceEntries, EXTERNAL_TRAINING_DAYS, date))
                .conferenceDays(workingTimeFilterHelper.getAbsenceTimesForEmployee(absenceEntries, CONFERENCE_DAYS, date))
                .maternityProtectionDays(workingTimeFilterHelper.getAbsenceTimesForEmployee(absenceEntries, MATERNITY_PROTECTION_DAYS, date))
                .fatherMonthDays(workingTimeFilterHelper.getAbsenceTimesForEmployee(absenceEntries, FATHER_MONTH_DAYS, date))
                .paidSpecialLeaveDays(workingTimeFilterHelper.getAbsenceTimesForEmployee(absenceEntries, PAID_SPECIAL_LEAVE_DAYS, date))
                .nonPaidVacationDays(workingTimeFilterHelper.getAbsenceTimesForEmployee(absenceEntries, NON_PAID_VACATION_DAYS, date))
                .paidSickLeave(workingTimeFilterHelper.getAbsenceTimesForEmployee(absenceEntries, PAID_SICK_LEAVE, date))
                .vacationDayBalance(personioEmployeesService.getVacationDayBalance(employee.getEmail()))
                .overtime(workingTimeFilterHelper.getOvertimeforEmployee(employee, billableEntries))
                .build();
    }

    private boolean isAnyStepEntryDone(Map.Entry<String, List<StepEntry>> entry) {
        return entry.getValue().stream().anyMatch(this::isStepEntryDone);
    }

    private boolean isStepEntryDone(StepEntry stepEntry) {
        return stepEntry.getState() == EmployeeState.DONE;
    }







}
