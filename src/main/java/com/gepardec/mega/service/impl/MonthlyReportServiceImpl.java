package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.StepName;
import com.gepardec.mega.domain.model.monthlyreport.JourneyWarning;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntryWarning;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.notification.mail.dates.OfficeCalendarUtil;
import com.gepardec.mega.rest.model.MappedTimeWarningDTO;
import com.gepardec.mega.rest.model.PmProgressDto;
import com.gepardec.mega.service.api.CommentService;
import com.gepardec.mega.service.api.MonthlyReportService;
import com.gepardec.mega.service.api.StepEntryService;
import com.gepardec.mega.service.helper.WarningCalculator;
import com.gepardec.mega.service.mapper.TimeWarningMapper;
import com.gepardec.mega.zep.ZepService;
import de.provantis.zep.FehlzeitType;
import de.provantis.zep.ProjektzeitType;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private LocalDate currentMonthYear;

    @Inject
    ZepService zepService;

    @Inject
    WarningCalculator warningCalculator;

    @Inject
    CommentService commentService;

    @Inject
    StepEntryService stepEntryService;

    @Inject
    TimeWarningMapper timeWarningMapper;

    @Override
    public MonthlyReport getMonthendReportForUser(final String userId) {
        final LocalDate date = Optional.ofNullable(zepService.getEmployee(userId))
                .flatMap(emp -> Optional.ofNullable(emp.getReleaseDate()))
                .filter(this::checkReleaseDate)
                .map(releaseDate -> LocalDate.parse(Objects.requireNonNull(releaseDate)).plusMonths(1))
                .orElse(null);

        return getMonthendReportForUser(userId, date);
    }

    private boolean checkReleaseDate(String releaseDate) {
        try {
            LocalDate.parse(Objects.requireNonNull(releaseDate));
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    @Override
    public MonthlyReport getMonthendReportForUser(String userId, LocalDate date) {
        Employee employee = zepService.getEmployee(userId);

        // Wenn kein date null, dann 1. Tag von aktuellen Monat (Logik wie bei StepEntryServiceImpl.parseReleaseDate)
        currentMonthYear = Optional.ofNullable(date).orElse(DateUtils.getFirstDayOfCurrentMonth());

        return buildMonthlyReport(employee,
                zepService.getProjectTimes(employee, date),
                zepService.getBillableForEmployee(employee, date),
                zepService.getAbsenceForEmployee(employee, date),
                stepEntryService.findEmployeeCheckState(employee, date),
                stepEntryService.findEmployeeInternalCheckState(employee, date));
    }

    private MonthlyReport buildMonthlyReport(Employee employee, List<ProjectEntry> projectEntries,
                                             List<ProjektzeitType> billableEntries, List<FehlzeitType> absenceEntries,
                                             Optional<Pair<EmployeeState, String>> employeeCheckState,
                                             Optional<EmployeeState> internalCheckState) {
        final List<JourneyWarning> journeyWarnings = warningCalculator.determineJourneyWarnings(projectEntries);
        final List<TimeWarning> timeWarnings = warningCalculator.determineTimeWarnings(projectEntries);
        timeWarnings.addAll(warningCalculator.determineNoTimeEntries(employee, projectEntries, absenceEntries));
        timeWarnings.sort(Comparator.comparing(ProjectEntryWarning::getDate));

        int year = currentMonthYear.getYear();
        int month = currentMonthYear.getMonthValue();

        List<Comment> comments = commentService.findCommentsForEmployee(employee, getFirstDayOfMonth(year, month), getLastDayOfMonth(year, month));

        final List<PmProgressDto> pmProgressDtos = Optional.ofNullable(employee)
                .map(empl -> stepEntryService.findAllOwnedAndUnassignedStepEntriesForPMProgress(empl.getEmail(), empl.getReleaseDate()))
                .orElse(Collections.emptyList())
                .stream()
                .map(PmProgressDto::ofStepEntry)
                .collect(Collectors.toList());

        List<StepEntry> allOwnedAndUnassignedStepEntriesForOtherChecks = stepEntryService.findAllOwnedAndUnassignedStepEntriesForOtherChecks(employee, currentMonthYear);

        Map<String, List<StepEntry>> controlTimeEvidencesStepsByProjects = allOwnedAndUnassignedStepEntriesForOtherChecks.stream()
                .filter(se -> StepName.CONTROL_TIME_EVIDENCES.name().equals(se.getStep().getName()))
                .collect(Collectors.groupingBy(StepEntry::getProject));

        // mind. 1 Projektleiter muss Employee auf Done gesetzt haben, bei Step 4 (Control Time Evidences)
        boolean controlTimeEvidencesDone = controlTimeEvidencesStepsByProjects.entrySet().stream()
                .allMatch(this::isAnyStepEntryDone);


        final boolean otherChecksDone = controlTimeEvidencesDone &&
                allOwnedAndUnassignedStepEntriesForOtherChecks.stream()
                        .filter(se -> !StepName.CONTROL_TIME_EVIDENCES.name().equals(se.getStep().getName()))
                        .allMatch(this::isStepEntryDone);

        List<MappedTimeWarningDTO> mappedTimeWarnings = timeWarningMapper.map(timeWarnings);

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
                .otherChecksDone(otherChecksDone)
                .billableTime(zepService.getBillableTimesForEmployee(billableEntries, employee))
                .totalWorkingTime(zepService.getTotalWorkingTimeForEmployee(billableEntries, employee))
                .compensatoryDays(getAbsenceTimesForEmployee(absenceEntries, COMPENSATORY_DAYS))
                .homeofficeDays(getAbsenceTimesForEmployee(absenceEntries, HOME_OFFICE_DAYS))
                .vacationDays(getAbsenceTimesForEmployee(absenceEntries, VACATION_DAYS))
                .nursingDays(getAbsenceTimesForEmployee(absenceEntries, NURSING_DAYS))
                .maternityLeaveDays(getAbsenceTimesForEmployee(absenceEntries, MATERNITY_LEAVE_DAYS))
                .externalTrainingDays(getAbsenceTimesForEmployee(absenceEntries, EXTERNAL_TRAINING_DAYS))
                .conferenceDays(getAbsenceTimesForEmployee(absenceEntries, CONFERENCE_DAYS))
                .maternityProtectionDays(getAbsenceTimesForEmployee(absenceEntries, MATERNITY_PROTECTION_DAYS))
                .fatherMonthDays(getAbsenceTimesForEmployee(absenceEntries, FATHER_MONTH_DAYS))
                .paidSpecialLeaveDays(getAbsenceTimesForEmployee(absenceEntries, PAID_SPECIAL_LEAVE_DAYS))
                .nonPaidVacationDays(getAbsenceTimesForEmployee(absenceEntries, NON_PAID_VACATION_DAYS))
                .paidSickLeave(getAbsenceTimesForEmployee(absenceEntries, PAID_SICK_LEAVE))
                .build();
    }

    private boolean isAnyStepEntryDone(Map.Entry<String, List<StepEntry>> entry) {
        return entry.getValue().stream().anyMatch(this::isStepEntryDone);
    }

    private boolean isStepEntryDone(StepEntry stepEntry) {
        return stepEntry.getState() == EmployeeState.DONE;
    }

    private int getAbsenceTimesForEmployee(@Nonnull List<FehlzeitType> fehlZeitTypeList, String absenceType) {
        return (int) fehlZeitTypeList.stream()
                .filter(fzt -> fzt.getFehlgrund().equals(absenceType))
                .filter(FehlzeitType::isGenehmigt)
                .map(this::trimDurationToCurrentMonth)
                .mapToLong(ftl -> OfficeCalendarUtil.getWorkingDaysBetween(LocalDate.parse(ftl.getStartdatum()), LocalDate.parse(ftl.getEnddatum())).size())
                .sum();
    }

    private FehlzeitType trimDurationToCurrentMonth(FehlzeitType fehlzeit) {
        if (LocalDate.parse(fehlzeit.getEnddatum()).getMonthValue() > currentMonthYear.getMonthValue()) {
            fehlzeit.setEnddatum(currentMonthYear.with(TemporalAdjusters.lastDayOfMonth()).toString());
        }
        if (LocalDate.parse(fehlzeit.getStartdatum()).getMonthValue() < currentMonthYear.getMonthValue()) {
            fehlzeit.setStartdatum(currentMonthYear.with(TemporalAdjusters.firstDayOfMonth()).toString());
        }
        return fehlzeit;
    }
}
