package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
                new InvalidWorkingLocationInJourneyCalculator(),
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
        return List.copyOf(warnings);
    }
}
