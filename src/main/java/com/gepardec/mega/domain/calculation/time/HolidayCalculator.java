package com.gepardec.mega.domain.calculation.time;

import com.gepardec.mega.domain.calculation.AbstractTimeWarningCalculationStrategy;
import com.gepardec.mega.domain.calculation.WarningCalculationStrategy;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarningType;
import com.gepardec.mega.notification.mail.dates.OfficeCalendarUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HolidayCalculator extends AbstractTimeWarningCalculationStrategy implements WarningCalculationStrategy<TimeWarning> {
    @Override
    public List<TimeWarning> calculate(List<ProjectEntry> projectEntries) {
        List<TimeWarning> warnings = new ArrayList<>();

        projectEntries.forEach(entry -> {
            if (OfficeCalendarUtil.isHoliday(entry.getDate())) {
                warnings.add(createTimeWarning(entry.getDate()));
            }
        });

        return warnings;
    }

    private TimeWarning createTimeWarning(final LocalDate date) {
        TimeWarning timeWarning = new TimeWarning();
        timeWarning.setDate(date);
        timeWarning.getWarningTypes().add(TimeWarningType.HOLIDAY);

        return timeWarning;
    }
}
