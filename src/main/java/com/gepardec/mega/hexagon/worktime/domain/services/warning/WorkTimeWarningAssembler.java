package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.EMPTY_ENTRY_LIST;

public class WorkTimeWarningAssembler {
    private final List<WorkTimeWarningCalculator> calculators;
    private final NoEntryWarningCalculator noEntryCalculator;

    public WorkTimeWarningAssembler() {
        this(List.of(
                new CoreWorkingHoursCalculator(),
                new TimeOverlapCalculator(),
                new HolidayCalculator(),
                new WeekendCalculator(),
                new DoctorAppointmentCalculator(),
                new ExceededMaximumWorkingHoursPerDayCalculator(),
                new InsufficientRestCalculator(),
                new InsufficientBreakCalculator(),
                new InvalidJourneyCalculator(),
                new InvalidWorkingLocationCalculator(),
                new LocationRelevantSetJourneyCalculator()), new NoEntryWarningCalculator());
    }

    WorkTimeWarningAssembler(List<WorkTimeWarningCalculator> calculators, NoEntryWarningCalculator noEntryCalculator) {
        this.calculators = List.copyOf(calculators);
        this.noEntryCalculator = noEntryCalculator;
    }

    public List<WorkTimeWarning> assemble(WorkTimeBookings bookings, Set<LocalDate> expectedWorkingDays,
                                          Set<LocalDate> excusedDates, LocalDate today) {
        if (bookings.isEmpty()) {
            return List.of(new WorkTimeWarning(null, EMPTY_ENTRY_LIST, null));
        }
        List<WorkTimeWarning> warnings = new ArrayList<>();
        calculators.forEach(calculator -> warnings.addAll(calculator.calculate(bookings)));
        warnings.addAll(noEntryCalculator.calculate(expectedWorkingDays, bookings.bookedDates(), excusedDates, today));
        return distinctPerTypeAndDate(warnings);
    }

    private static List<WorkTimeWarning> distinctPerTypeAndDate(List<WorkTimeWarning> warnings) {
        return List.copyOf(warnings.stream()
                .collect(Collectors.toMap(
                        warning -> new TypeAndDate(warning.type(), warning.date()),
                        Function.identity(),
                        (first, duplicate) -> first,
                        LinkedHashMap::new))
                .values());
    }

    private record TypeAndDate(WorkTimeWarningType type, LocalDate date) {
    }
}
