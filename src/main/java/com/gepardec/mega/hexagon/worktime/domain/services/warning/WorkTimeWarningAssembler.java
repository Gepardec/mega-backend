package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WarningRule;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRuleSettings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEvaluation;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.EMPTY_ENTRY_LIST;

public class WorkTimeWarningAssembler {
    private final List<WorkTimeWarningCalculator> calculators;

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
                new LocationRelevantSetJourneyCalculator(),
                new InternalNoteConventionCalculator(),
                new NoEntryWarningCalculator(),
                new NonBillableOnBillableProjectCalculator(),
                new TravelAllowanceCalculator(),
                new MealEntryCalculator()));
    }

    WorkTimeWarningAssembler(List<WorkTimeWarningCalculator> calculators) {
        this.calculators = List.copyOf(calculators);
    }

    List<WarningRule> rules() {
        return calculators.stream().map(WorkTimeWarningCalculator::rule).toList();
    }

    public List<WorkTimeWarning> assemble(WorkTimeEvaluation evaluation, WarningRuleSettings settings) {
        if (evaluation.bookings().isEmpty()) {
            return List.of(new WorkTimeWarning(null, EMPTY_ENTRY_LIST, null));
        }
        List<WorkTimeWarning> warnings = new ArrayList<>();
        calculators.stream()
                .filter(calculator -> settings.isActive(calculator.rule()))
                .forEach(calculator -> calculator.calculate(evaluation).stream()
                        .filter(warning -> settings.appliesOn(calculator.rule(), warning.date()))
                        .forEach(warnings::add));
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
