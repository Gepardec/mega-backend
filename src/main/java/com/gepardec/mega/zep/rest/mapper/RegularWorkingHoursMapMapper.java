package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.rest.entity.ZepRegularWorkingTimes;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class RegularWorkingHoursMapMapper {

    public static Map<DayOfWeek, Duration> map(ZepRegularWorkingTimes zepRegularWorkingTimes) {
        Map<DayOfWeek, Duration> regularWorkingHours = new HashMap<>();
        if (zepRegularWorkingTimes != null) {
            regularWorkingHours.put(DayOfWeek.MONDAY, Duration.ofHours(zepRegularWorkingTimes.getMonday().longValue()));
            regularWorkingHours.put(DayOfWeek.TUESDAY, Duration.ofHours(zepRegularWorkingTimes.getTuesday().longValue()));
            regularWorkingHours.put(DayOfWeek.WEDNESDAY, Duration.ofHours(zepRegularWorkingTimes.getWednesday().longValue()));
            regularWorkingHours.put(DayOfWeek.THURSDAY, Duration.ofHours(zepRegularWorkingTimes.getThursday().longValue()));
            regularWorkingHours.put(DayOfWeek.FRIDAY, Duration.ofHours(zepRegularWorkingTimes.getFriday().longValue()));
            regularWorkingHours.put(DayOfWeek.SATURDAY, Duration.ofHours(zepRegularWorkingTimes.getSaturday().longValue()));
            regularWorkingHours.put(DayOfWeek.SUNDAY, Duration.ofHours(zepRegularWorkingTimes.getSunday().longValue()));
        }
        return regularWorkingHours;
    }
}
