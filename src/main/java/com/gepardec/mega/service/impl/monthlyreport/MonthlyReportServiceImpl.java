package com.gepardec.mega.service.impl.monthlyreport;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.monthlyreport.JourneyWarning;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.rest.model.PmProgress;
import com.gepardec.mega.service.api.comment.CommentService;
import com.gepardec.mega.service.api.monthlyreport.MonthlyReportService;
import com.gepardec.mega.service.api.stepentry.StepEntryService;
import com.gepardec.mega.zep.ZepService;
import de.provantis.zep.FehlzeitType;
import de.provantis.zep.ProjektzeitType;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestScoped
public class MonthlyReportServiceImpl implements MonthlyReportService {

    public static final String COMPENSATORY_DAYS = "FA";
    public static final String HOME_OFFICE_DAYS = "HO";
    public static final String VACATION_DAYS = "UB";

    @Inject
    ZepService zepService;

    @Inject
    WarningCalculator warningCalculator;

    @Inject
    CommentService commentService;

    @Inject
    StepEntryService stepEntryService;

    @Override
    public MonthlyReport getMonthendReportForUser(final String userId) {
        Employee employee = zepService.getEmployee(userId);
        final LocalDate date;

        if (employee != null && employee.releaseDate() != null) {
            date = LocalDate.parse(employee.releaseDate()).plusMonths(1);
        } else {
            date = null;
        }
        return getMonthendReportForUser(userId, date);
    }

    @Override
    public MonthlyReport getMonthendReportForUser(String userId, LocalDate date) {
        Employee employee = zepService.getEmployee(userId);

        List<FehlzeitType> absenceForEmployee = zepService.getAbsenceForEmployee(employee, date);
        absenceForEmployee = absenceForEmployee.stream()
                .map(fehlzeit -> trimDurationToCurrentMonth(fehlzeit, date))
                .collect(Collectors.toList());

        return buildMonthlyReport(employee, zepService.getProjectTimes(employee, date), zepService.getBillableForEmployee(employee, date), absenceForEmployee, stepEntryService.findEmployeeCheckState(employee, date));
    }

    @Override
    public boolean setOpenAndUnassignedStepEntriesDone(Employee employee, Long stepId) {
        LocalDate from = LocalDate.parse(DateUtils.getFirstDayOfFollowingMonth(employee.releaseDate()));
        LocalDate to = LocalDate.parse(DateUtils.getLastDayOfFollowingMonth(employee.releaseDate()));

        return stepEntryService.setOpenAndAssignedStepEntriesDone(employee, stepId, from, to);
    }

    private FehlzeitType trimDurationToCurrentMonth(FehlzeitType fehlzeitType, LocalDate date) {
        if (LocalDate.parse(fehlzeitType.getEnddatum()).getMonthValue() > date.getMonthValue()) {
            fehlzeitType.setEnddatum(date.with(TemporalAdjusters.lastDayOfMonth()).toString());
        } else if (LocalDate.parse(fehlzeitType.getStartdatum()).getMonthValue() < date.getMonthValue()) {
            fehlzeitType.setStartdatum(date.with(TemporalAdjusters.firstDayOfMonth()).toString());
        }
        return fehlzeitType;
    }

    private MonthlyReport buildMonthlyReport(Employee employee, List<ProjectEntry> projectEntries, List<ProjektzeitType> billableEntries, List<FehlzeitType> absenceEntries, Optional<EmployeeState> employeeCheckState) {
        final List<JourneyWarning> journeyWarnings = warningCalculator.determineJourneyWarnings(projectEntries);
        final List<TimeWarning> timeWarnings = warningCalculator.determineTimeWarnings(projectEntries);

        final List<Comment> comments = new ArrayList<>();
        List<PmProgress> pmProgresses = new ArrayList<>();
        if (employee != null) {
            LocalDate fromDate = LocalDate.parse(DateUtils.getFirstDayOfFollowingMonth(employee.releaseDate()));
            LocalDate toDate = LocalDate.parse(DateUtils.getLastDayOfFollowingMonth(employee.releaseDate()));
            comments.addAll(commentService.findCommentsForEmployee(employee, fromDate, toDate));

            final List<StepEntry> allOwnedStepEntriesForPMProgress = stepEntryService.findAllOwnedAndUnassignedStepEntriesForPMProgress(employee.email(), employee.releaseDate());
            allOwnedStepEntriesForPMProgress
                    .forEach(stepEntry -> pmProgresses.add(
                            PmProgress.builder()
                                    .project(stepEntry.getProject())
                                    .assigneeEmail(stepEntry.getAssignee().getEmail())
                                    .firstname(stepEntry.getAssignee().getFirstname())
                                    .lastname(stepEntry.getAssignee().getLastname())
                                    .state(stepEntry.getState())
                                    .stepId(stepEntry.getStep().getId())
                                    .build()
                    ));
        }

        final boolean isAssigned = employeeCheckState.isPresent();

        final List<StepEntry> allOwnedAndAssignedStepEntries = stepEntryService.findAllOwnedAndUnassignedStepEntriesForOtherChecks(employee);
        final boolean otherChecksDone = allOwnedAndAssignedStepEntries.stream().allMatch(stepEntry -> stepEntry.getState() == EmployeeState.DONE);

        return MonthlyReport.builder()
                .employee(employee)
                .timeWarnings(timeWarnings)
                .journeyWarnings(journeyWarnings)
                .comments(comments)
                .employeeCheckState(employeeCheckState.orElse(EmployeeState.OPEN))
                .isAssigned(isAssigned)
                .employeeProgresses(pmProgresses)
                .otherChecksDone(otherChecksDone)
                .billableTime(zepService.getBillableTimesForEmployee(billableEntries, employee, true))
                .totalWorkingTime(zepService.getTotalWorkingTimeForEmployee(billableEntries, employee))
                .compensatoryDays(getAbsenceTimesForEmployee(absenceEntries, employee, COMPENSATORY_DAYS))
                .homeofficeDays(getAbsenceTimesForEmployee(absenceEntries, employee, HOME_OFFICE_DAYS))
                .vacationDays(getAbsenceTimesForEmployee(absenceEntries, employee, VACATION_DAYS))
                .build();
    }

    private int getAbsenceTimesForEmployee(@Nonnull List<FehlzeitType> fehlZeitTypeList, @Nonnull Employee employee, String absenceType) {
        int totalAbsence = fehlZeitTypeList.stream()
                .filter(fzt -> fzt.getFehlgrund().equals(absenceType))
                .filter(FehlzeitType::isGenehmigt)
                .map(ftl -> (Period.between(LocalDate.parse(ftl.getStartdatum()), LocalDate.parse(ftl.getEnddatum()))))
                .mapToInt(Period::getDays)
                .sum();

        // Conditionally add missing day because Period.between() does not include the given endDate
        return totalAbsence > 0 ? totalAbsence + 1 : totalAbsence;
    }
}
