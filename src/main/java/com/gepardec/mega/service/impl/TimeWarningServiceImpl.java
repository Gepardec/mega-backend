package com.gepardec.mega.service.impl;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.WorkTimeBookingWarning;
import com.gepardec.mega.domain.model.monthlyreport.JourneyWarning;
import com.gepardec.mega.domain.model.monthlyreport.JourneyWarningType;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntryWarning;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarningType;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.service.api.TimeWarningService;
import com.gepardec.mega.service.helper.WarningCalculatorsManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@ApplicationScoped
public class TimeWarningServiceImpl implements TimeWarningService {

    @Inject
    WarningCalculatorsManager warningCalculatorsManager;

    @Override
    public List<WorkTimeBookingWarning> getAllTimeWarningsForEmployeeAndMonth(
            List<AbsenceTime> absences,
            List<ProjectEntry> projectEntries,
            Employee employee) {

        List<TimeWarning> timeWarnings = collectTimeWarnings(projectEntries, employee, absences);
        List<JourneyWarning> journeyWarnings = warningCalculatorsManager.determineJourneyWarnings(projectEntries);

        List<WorkTimeBookingWarning> result = new ArrayList<>();
        result.addAll(groupTimeWarningsByType(timeWarnings));
        result.addAll(groupJourneyWarningsByType(journeyWarnings));

        return result;
    }

    private List<TimeWarning> collectTimeWarnings(List<ProjectEntry> projectEntries, Employee employee, List<AbsenceTime> absences) {
        List<TimeWarning> warnings = new ArrayList<>(warningCalculatorsManager.determineTimeWarnings(projectEntries));
        warnings.addAll(warningCalculatorsManager.determineNoTimeEntries(employee, projectEntries, absences));
        warnings.sort(Comparator.comparing(ProjectEntryWarning::getDate));
        return warnings;
    }

    private List<WorkTimeBookingWarning> groupTimeWarningsByType(List<TimeWarning> timeWarnings) {
        return Stream.of(TimeWarningType.values())
                .map(type -> createTimeWarningGroup(timeWarnings, type))
                .flatMap(List::stream)
                .toList();
    }

    private List<WorkTimeBookingWarning> groupJourneyWarningsByType(List<JourneyWarning> journeyWarnings) {
        return Stream.of(JourneyWarningType.values())
                .map(type -> createJourneyWarningGroup(journeyWarnings, type))
                .flatMap(List::stream)
                .toList();
    }

    private List<WorkTimeBookingWarning> createTimeWarningGroup(List<TimeWarning> warnings, TimeWarningType type) {
        List<TimeWarning> warningsOfType = warnings.stream()
                .filter(warning -> warning.getWarningTypes().contains(type))
                .toList();

        if (warningsOfType.isEmpty()) {
            return List.of();
        }

        // Month-level warnings (like EMPTY_ENTRY_LIST) have no date
        if (isMonthLevelWarning(type)) {
            return List.of(WorkTimeBookingWarning.builder()
                    .name(type.name())
                    .warningDates(List.of())
                    .build());
        }

        // Date-specific warnings need dates
        List<WorkTimeBookingWarning.WarningDate> dates = warningsOfType.stream()
                .filter(warning -> warning.getDate() != null)
                .map(warning -> new WorkTimeBookingWarning.WarningDate(
                        DateUtils.formatDate(warning.getDate()),
                        extractHoursForWarningType(warning, type)
                ))
                .toList();

        if (dates.isEmpty()) {
            return List.of();
        }

        return List.of(WorkTimeBookingWarning.builder()
                .name(type.name())
                .warningDates(dates)
                .build());
    }

    private boolean isMonthLevelWarning(TimeWarningType type) {
        return type == TimeWarningType.EMPTY_ENTRY_LIST;
    }

    private List<WorkTimeBookingWarning> createJourneyWarningGroup(List<JourneyWarning> warnings, JourneyWarningType type) {
        List<WorkTimeBookingWarning.WarningDate> dates = warnings.stream()
                .filter(warning -> warning.getWarningTypes().contains(type))
                .filter(warning -> warning.getDate() != null)
                .map(warning -> new WorkTimeBookingWarning.WarningDate(
                        DateUtils.formatDate(warning.getDate()),
                        null
                ))
                .toList();

        if (dates.isEmpty()) {
            return List.of();
        }

        return List.of(WorkTimeBookingWarning.builder()
                .name(type.name())
                .warningDates(dates)
                .build());
    }

    private Double extractHoursForWarningType(TimeWarning warning, TimeWarningType type) {
        return switch (type) {
            case MISSING_BREAK_TIME -> warning.getMissingBreakTime();
            case MISSING_REST_TIME -> warning.getMissingRestTime();
            case EXCESS_WORKING_TIME_PRESENT -> warning.getExcessWorkTime();
            default -> null;
        };
    }
}