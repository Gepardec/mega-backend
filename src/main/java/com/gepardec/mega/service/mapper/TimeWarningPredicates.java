package com.gepardec.mega.service.mapper;

import com.gepardec.mega.domain.model.monthlyreport.MappedTimeWarningTypes;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarningType;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.function.Predicate;

@ApplicationScoped
public class TimeWarningPredicates {

    public Map<MappedTimeWarningTypes, Predicate<TimeWarning>> getPredicateMap() {
        return predicateMap;
    }

    private final Map<MappedTimeWarningTypes,Predicate<TimeWarning>> predicateMap = Map.ofEntries(
            Map.entry(MappedTimeWarningTypes.MISSINGREST, missingRestPredicate),
            Map.entry(MappedTimeWarningTypes.MISSINGBREAK, missingBreakPredicate),
            Map.entry(MappedTimeWarningTypes.EXCESSWORK, excessWorkPredicate),
            Map.entry(MappedTimeWarningTypes.DOCTOR, wrongDoctorAppointmentPredicate),
            Map.entry(MappedTimeWarningTypes.NO_TIME_ENTRY, noTimeEntryPredicate),
            Map.entry(MappedTimeWarningTypes.TIME_OVERLAP, timeOverlapPredicate),
            Map.entry(MappedTimeWarningTypes.OUTSIDE_CORE_WORKING_TIME, outsideCorePredicate),
            Map.entry(MappedTimeWarningTypes.EMPTY_ENTRY_LIST, emptyEntryListPredicate),
            Map.entry(MappedTimeWarningTypes.HOLIDAY, holidayPredicate),
            Map.entry(MappedTimeWarningTypes.WEEKEND, weekendPredicate)
    );

    private static final Predicate<TimeWarning> missingRestPredicate = w -> {
        if (w.getMissingRestTime() != null) {
            return w.getMissingRestTime().compareTo(0.0) > 0;
        }
        return false;
    };

    private static final Predicate<TimeWarning> missingBreakPredicate = w -> {
        if (w.getMissingBreakTime() != null) {
            return w.getMissingBreakTime().compareTo(0.0) > 0;
        }
        return false;
    };

    private static final Predicate<TimeWarning> excessWorkPredicate = w -> {
        if (w.getExcessWorkTime() != null) {
            return w.getExcessWorkTime().compareTo(0.0) > 0;
        }
        return false;
    };

    private static final Predicate<TimeWarning> wrongDoctorAppointmentPredicate = w -> {
        if (w.getWarningTypes() != null) {
            return w.getWarningTypes().contains(TimeWarningType.WRONG_DOCTOR_APPOINTMENT);
        }
        return false;
    };

    private static final Predicate<TimeWarning> noTimeEntryPredicate = w -> {
        if (w.getWarningTypes() != null) {
            return w.getWarningTypes().contains(TimeWarningType.NO_TIME_ENTRY);
        }
        return false;
    };

    private static final Predicate<TimeWarning> outsideCorePredicate = w -> {
        if (w.getWarningTypes() != null) {
            return w.getWarningTypes().contains(TimeWarningType.OUTSIDE_CORE_WORKING_TIME);
        }
        return false;
    };

    private static final Predicate<TimeWarning> timeOverlapPredicate = w -> {
        if (w.getWarningTypes() != null) {
            return w.getWarningTypes().contains(TimeWarningType.TIME_OVERLAP);
        }
        return false;
    };

    private static final Predicate<TimeWarning> emptyEntryListPredicate = w -> {
        if (w.getWarningTypes() != null) {
            return w.getWarningTypes().contains(TimeWarningType.EMPTY_ENTRY_LIST);
        }
        return false;
    };

    private static final Predicate<TimeWarning> holidayPredicate = w -> {
        if (w.getWarningTypes() != null) {
            return w.getWarningTypes().contains(TimeWarningType.HOLIDAY);
        }
        return false;
    };

      private static final Predicate<TimeWarning> weekendPredicate = w -> {
        if (w.getWarningTypes() != null) {
            return w.getWarningTypes().contains(TimeWarningType.WEEKEND);
        }
        return false;
    };
}
