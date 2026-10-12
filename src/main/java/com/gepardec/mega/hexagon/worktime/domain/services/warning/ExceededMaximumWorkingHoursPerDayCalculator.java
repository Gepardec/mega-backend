package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;

import java.util.ArrayList;
import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.EXCESS_WORKING_TIME_PRESENT;

public class ExceededMaximumWorkingHoursPerDayCalculator implements WorkTimeWarningCalculator {
    static final double MAX_HOURS_A_DAY = 10d;

    @Override
    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings) {
        List<WorkTimeWarning> warnings = new ArrayList<>();
        bookings.contributingToWorkingTime().byDate().forEach((date, dayBookings) -> {
            double duration = dayBookings.totalDuration().toMinutes() / 60d;
            if (duration > MAX_HOURS_A_DAY) {
                warnings.add(new WorkTimeWarning(date, EXCESS_WORKING_TIME_PRESENT, duration - MAX_HOURS_A_DAY));
            }
        });
        return warnings;
    }
}
