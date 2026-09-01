package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.shared.domain.util.OfficeCalendarUtil;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;

import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.HOLIDAY;

public class HolidayCalculator implements WorkTimeWarningCalculator {
    @Override
    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings) {
        return bookings.bookedDates().stream()
                .filter(OfficeCalendarUtil::isHoliday)
                .map(date -> new WorkTimeWarning(date, HOLIDAY, null))
                .toList();
    }
}
