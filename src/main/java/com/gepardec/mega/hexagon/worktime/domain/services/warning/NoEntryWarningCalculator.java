package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WarningRule;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEvaluation;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.NO_TIME_ENTRY;

public class NoEntryWarningCalculator implements WorkTimeWarningCalculator {
    @Override
    public WarningRule rule() {
        return WarningRule.NO_TIME_ENTRY;
    }

    @Override
    public List<WorkTimeWarning> calculate(WorkTimeEvaluation evaluation) {
        Set<LocalDate> bookedDates = evaluation.bookings().bookedDates();
        return evaluation.expectedWorkingDays().stream()
                .filter(date -> !bookedDates.contains(date))
                .filter(date -> !evaluation.excusedDates().contains(date))
                .filter(date -> date.isBefore(evaluation.evaluationDate()))
                .sorted()
                .map(date -> new WorkTimeWarning(date, NO_TIME_ENTRY, null))
                .toList();
    }
}
