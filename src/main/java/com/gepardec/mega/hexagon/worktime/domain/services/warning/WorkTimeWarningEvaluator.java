package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WarningRule;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRuleSettings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEvaluation;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarnings;

import java.util.List;

/**
 * Runs every active warning rule over a month's evaluation.
 */
public class WorkTimeWarningEvaluator {
    private final List<WorkTimeWarningCalculator> calculators;

    public WorkTimeWarningEvaluator() {
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

    WorkTimeWarningEvaluator(List<WorkTimeWarningCalculator> calculators) {
        this.calculators = List.copyOf(calculators);
    }

    List<WarningRule> rules() {
        return calculators.stream().map(WorkTimeWarningCalculator::rule).toList();
    }

    public WorkTimeWarnings evaluate(WorkTimeEvaluation evaluation, WarningRuleSettings settings) {
        if (evaluation.bookings().isEmpty()) {
            return WorkTimeWarnings.noEntries();
        }
        List<WorkTimeWarning> warnings = calculators.stream()
                .filter(calculator -> settings.isActive(calculator.rule()))
                .flatMap(calculator -> calculator.calculate(evaluation).stream()
                        .filter(warning -> settings.appliesOn(calculator.rule(), warning.date())))
                .toList();
        return new WorkTimeWarnings(warnings);
    }
}
