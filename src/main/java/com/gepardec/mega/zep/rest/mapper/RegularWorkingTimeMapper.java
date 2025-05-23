package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.RegularWorkingTime;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class RegularWorkingTimeMapper implements Mapper<RegularWorkingTime, ZepRegularWorkingTimes> {

    @Override
    public RegularWorkingTime map(ZepRegularWorkingTimes zepRegularWorkingTimes) {
        return new RegularWorkingTime(
                Optional.ofNullable(zepRegularWorkingTimes.startDate()).map(LocalDateTime::toLocalDate).orElse(null),
                mapDays(zepRegularWorkingTimes)
        );
    }

    private Map<DayOfWeek, Duration> mapDays(ZepRegularWorkingTimes zepRegularWorkingTime) {
        Map<DayOfWeek, Duration> regularWorkingHours = new EnumMap<>(DayOfWeek.class);
        regularWorkingHours.put(DayOfWeek.MONDAY, toDuration(zepRegularWorkingTime.monday()));
        regularWorkingHours.put(DayOfWeek.TUESDAY, toDuration(zepRegularWorkingTime.tuesday()));
        regularWorkingHours.put(DayOfWeek.WEDNESDAY, toDuration(zepRegularWorkingTime.wednesday()));
        regularWorkingHours.put(DayOfWeek.THURSDAY, toDuration(zepRegularWorkingTime.thursday()));
        regularWorkingHours.put(DayOfWeek.FRIDAY, toDuration(zepRegularWorkingTime.friday()));
        regularWorkingHours.put(DayOfWeek.SATURDAY, toDuration(zepRegularWorkingTime.saturday()));
        regularWorkingHours.put(DayOfWeek.SUNDAY, toDuration(zepRegularWorkingTime.sunday()));
        return regularWorkingHours;
    }

    private Duration toDuration(Double hours) {
        return Duration.ofHours(hours == null ? 0 : hours.longValue());
    }
}
