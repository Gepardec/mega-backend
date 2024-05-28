package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class RegularWorkingHoursMapMapper implements Mapper<Map<DayOfWeek, Duration>, ZepRegularWorkingTimes> {

    @Override
    public Map<DayOfWeek, Duration> map(ZepRegularWorkingTimes zepRegularWorkingTimes) {

        try{

            Map<DayOfWeek, Duration> regularWorkingHours = new EnumMap<>(DayOfWeek.class);

            if (zepRegularWorkingTimes != null) {
                regularWorkingHours.put(DayOfWeek.MONDAY, Duration.ofHours(zepRegularWorkingTimes.monday() == null ? 0 : zepRegularWorkingTimes.monday().longValue()));
                regularWorkingHours.put(DayOfWeek.TUESDAY, Duration.ofHours(zepRegularWorkingTimes.tuesday() == null ? 0 : zepRegularWorkingTimes.tuesday().longValue()));
                regularWorkingHours.put(DayOfWeek.WEDNESDAY, Duration.ofHours(zepRegularWorkingTimes.wednesday() == null ? 0 : zepRegularWorkingTimes.wednesday().longValue()));
                regularWorkingHours.put(DayOfWeek.THURSDAY, Duration.ofHours(zepRegularWorkingTimes.thursday() == null ? 0 : zepRegularWorkingTimes.thursday().longValue()));
                regularWorkingHours.put(DayOfWeek.FRIDAY, Duration.ofHours(zepRegularWorkingTimes.friday() == null ? 0 : zepRegularWorkingTimes.friday().longValue()));
                regularWorkingHours.put(DayOfWeek.SATURDAY, Duration.ofHours(zepRegularWorkingTimes.saturday() == null ? 0 : zepRegularWorkingTimes.saturday().longValue()));
                regularWorkingHours.put(DayOfWeek.SUNDAY, Duration.ofHours(zepRegularWorkingTimes.sunday() == null ? 0 : zepRegularWorkingTimes.sunday().longValue()));
            }
            return regularWorkingHours;
        }catch (Exception e){
            throw new ZepServiceException("While trying to map ZepRegularWorkingTimes to Map<DayOfWeek, Duration>, an error occurred", e);
        }
    }
}
