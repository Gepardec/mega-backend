package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WarningRule;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEvaluation;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.services.TripDays;

import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.MEAL_ENTRY_MISSING;

public class MealEntryCalculator implements WorkTimeWarningCalculator {
    @Override
    public WarningRule rule() {
        return WarningRule.MEAL_ENTRY;
    }

    @Override
    public List<WorkTimeWarning> calculate(WorkTimeEvaluation evaluation) {
        return TripDays.of(evaluation.bookings().journeys(), evaluation.payrollMonth()).stream()
                .filter(date -> date.isBefore(evaluation.evaluationDate()))
                .filter(date -> !evaluation.mealEntryDates().contains(date))
                .sorted()
                .map(date -> new WorkTimeWarning(date, MEAL_ENTRY_MISSING, null))
                .toList();
    }
}
