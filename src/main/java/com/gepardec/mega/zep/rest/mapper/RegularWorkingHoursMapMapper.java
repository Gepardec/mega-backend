package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.rest.entity.ZepRegularWorkingTimes;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class RegularWorkingHoursMapMapper implements Mapper<Map<DayOfWeek, Duration>, ZepRegularWorkingTimes> {

    @Override
    public Map<DayOfWeek, Duration> map(ZepRegularWorkingTimes zepRegularWorkingTimes) {
        Map<DayOfWeek, Duration> regularWorkingHours = new HashMap<>();

        if (zepRegularWorkingTimes != null) {
            regularWorkingHours.put(DayOfWeek.MONDAY, Duration.ofHours(zepRegularWorkingTimes.getMonday() == null ? 0 : zepRegularWorkingTimes.getMonday().longValue()));
            regularWorkingHours.put(DayOfWeek.TUESDAY, Duration.ofHours(zepRegularWorkingTimes.getTuesday() == null ? 0 : zepRegularWorkingTimes.getTuesday().longValue()));
            regularWorkingHours.put(DayOfWeek.WEDNESDAY, Duration.ofHours(zepRegularWorkingTimes.getWednesday() == null ? 0 : zepRegularWorkingTimes.getWednesday().longValue()));
            regularWorkingHours.put(DayOfWeek.THURSDAY, Duration.ofHours(zepRegularWorkingTimes.getThursday() == null ? 0 : zepRegularWorkingTimes.getThursday().longValue()));
            regularWorkingHours.put(DayOfWeek.FRIDAY, Duration.ofHours(zepRegularWorkingTimes.getFriday() == null ? 0 : zepRegularWorkingTimes.getFriday().longValue()));
            regularWorkingHours.put(DayOfWeek.SATURDAY, Duration.ofHours(zepRegularWorkingTimes.getSaturday() == null ? 0 : zepRegularWorkingTimes.getSaturday().longValue()));
            regularWorkingHours.put(DayOfWeek.SUNDAY, Duration.ofHours(zepRegularWorkingTimes.getSunday() == null ? 0 : zepRegularWorkingTimes.getSunday().longValue()));
        }
        return regularWorkingHours;
    }
}
