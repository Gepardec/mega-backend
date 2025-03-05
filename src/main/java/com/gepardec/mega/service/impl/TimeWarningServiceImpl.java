package com.gepardec.mega.service.impl;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.MonthlyWarning;
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

import static com.gepardec.mega.domain.model.monthlyreport.TimeWarningType.EMPTY_ENTRY_LIST;
import static com.gepardec.mega.domain.model.monthlyreport.TimeWarningType.EXCESS_WORKING_TIME_PRESENT;
import static com.gepardec.mega.domain.model.monthlyreport.TimeWarningType.HOLIDAY;
import static com.gepardec.mega.domain.model.monthlyreport.TimeWarningType.MISSING_BREAK_TIME;
import static com.gepardec.mega.domain.model.monthlyreport.TimeWarningType.MISSING_REST_TIME;
import static com.gepardec.mega.domain.model.monthlyreport.TimeWarningType.NO_TIME_ENTRY;
import static com.gepardec.mega.domain.model.monthlyreport.TimeWarningType.OUTSIDE_CORE_WORKING_TIME;
import static com.gepardec.mega.domain.model.monthlyreport.TimeWarningType.TIME_OVERLAP;
import static com.gepardec.mega.domain.model.monthlyreport.TimeWarningType.WEEKEND;
import static com.gepardec.mega.domain.model.monthlyreport.TimeWarningType.WRONG_DOCTOR_APPOINTMENT;

@ApplicationScoped
public class TimeWarningServiceImpl implements TimeWarningService {
    @Inject
    WarningCalculatorsManager warningCalculatorsManager;

    private static final List<TimeWarningType> timeWarningTypes = List.of(
            OUTSIDE_CORE_WORKING_TIME,
            TIME_OVERLAP,
            NO_TIME_ENTRY,
            EMPTY_ENTRY_LIST,
            HOLIDAY,
            WEEKEND,
            WRONG_DOCTOR_APPOINTMENT,
            MISSING_BREAK_TIME,
            MISSING_REST_TIME,
            EXCESS_WORKING_TIME_PRESENT
    );

    private static final List<JourneyWarningType> journeyWarningTypes = List.of(
            JourneyWarningType.BACK_MISSING,
            JourneyWarningType.TO_MISSING,
            JourneyWarningType.INVALID_WORKING_LOCATION
    );

    @Override
    public List<MonthlyWarning> getAllTimeWarningsForEmployeeAndMonth(List<AbsenceTime> absences, List<ProjectEntry> projectEntries, Employee employee) {
        final List<JourneyWarning> journeyWarnings = warningCalculatorsManager.determineJourneyWarnings(projectEntries);
        final List<TimeWarning> timeWarnings = warningCalculatorsManager.determineTimeWarnings(projectEntries);
        timeWarnings.addAll(warningCalculatorsManager.determineNoTimeEntries(employee, projectEntries, absences));
        timeWarnings.sort(Comparator.comparing(ProjectEntryWarning::getDate));
        List<MonthlyWarning> monthlyWarnings = new ArrayList<>();


        timeWarningTypes.forEach(warningType -> {
            MonthlyWarning warningEntry = createMonthlyTimeWarning(timeWarnings, warningType);
            if (warningEntry != null) {
                monthlyWarnings.add(warningEntry);
            }
        });

        journeyWarningTypes.forEach(warningType -> {
            MonthlyWarning warningEntry = createMonthlyJourneyWarning(journeyWarnings, warningType);
            if (warningEntry != null) {
                monthlyWarnings.add(warningEntry);
            }
        });
        return monthlyWarnings;
    }

    private MonthlyWarning createMonthlyTimeWarning(List<TimeWarning> timeWarnings, TimeWarningType warningType) {
        String name = "";
        List<String> datesWhenWarningsOccurred = new ArrayList<>();

        for (TimeWarning timeWarning : timeWarnings) {
            if (timeWarning.getWarningTypes().contains(warningType)) {
                if (name.isEmpty()) {
                    name = switch (warningType) {
                        case TIME_OVERLAP -> "Zeiten überschneiden sich";
                        case NO_TIME_ENTRY -> "Keine Zeit-Buchung vorhanden";
                        case OUTSIDE_CORE_WORKING_TIME -> "Zeit-Buchung außerhalb der Kernarbeitszeit";
                        case EMPTY_ENTRY_LIST -> "Keine Buchung oder Abwesenheit für dieses Monat";
                        case HOLIDAY -> "Zeit-Buchung an einem Feiertag";
                        case WEEKEND -> "Zeit-Buchung am Wochenende";
                        case WRONG_DOCTOR_APPOINTMENT -> "Falsche Zeit-Buchung für Arzttermin";
                        case MISSING_BREAK_TIME -> "Zu wenig Pausenzeit eingetragen";
                        case MISSING_REST_TIME -> "Ruhezeit wurde nicht eingehalten";
                        case EXCESS_WORKING_TIME_PRESENT -> "Zu viel Arbeitszeit gebucht";
                    };
                }
                if (timeWarning.getDate() != null) {
                    datesWhenWarningsOccurred.add(DateUtils.formatDate(timeWarning.getDate()));
                }
            }
        }

        // if we do not check this we get empty objects
        if (!name.isEmpty()) {
            return MonthlyWarning.builder()
                    .name(name)
                    .datesWhenWarningOccurred(datesWhenWarningsOccurred)
                    .build();
        }
        return null;
    }

    private MonthlyWarning createMonthlyJourneyWarning(List<JourneyWarning> journeyWarnings, JourneyWarningType warningType) {
        String name = "";
        List<String> datesWhenWarningsOccurred = new ArrayList<>();

        for (JourneyWarning journeyWarning : journeyWarnings) {
            if (journeyWarning.getWarningTypes().contains(warningType)) {
                if (name.isEmpty()) {
                    name = switch (warningType) {
                        case BACK_MISSING -> "Rückreise fehlt oder ist nach dem Zeitraum";
                        case TO_MISSING -> "Hinreise fehlt oder ist vor dem Zeitraum";
                        case INVALID_WORKING_LOCATION -> "Ungültiger Arbeitsort während einer Reise";
                        case LOCATION_RELEVANT_SET -> "Ort projektrelevant darf nur gesetzt sein, wenn die Reisezeit verrechnet wird";
                    };
                }
                if(journeyWarning.getDate() != null) {
                    datesWhenWarningsOccurred.add(DateUtils.formatDate(journeyWarning.getDate()));
                }
            }
        }

        if(!name.isEmpty()) {
            return MonthlyWarning.builder()
                    .name(name)
                    .datesWhenWarningOccurred(datesWhenWarningsOccurred)
                    .build();
        }
        return null;
    }
}

