package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEvaluation;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;

import java.util.List;

public interface BookingsWarningCalculator extends WorkTimeWarningCalculator {
    List<WorkTimeWarning> calculate(WorkTimeBookings bookings);

    @Override
    default List<WorkTimeWarning> calculate(WorkTimeEvaluation evaluation) {
        return calculate(evaluation.bookings());
    }
}
