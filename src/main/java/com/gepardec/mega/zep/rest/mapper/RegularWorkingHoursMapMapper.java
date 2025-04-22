package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.Range;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@ApplicationScoped
public class RegularWorkingHoursMapMapper implements Mapper<Map<Range<LocalDate>,Map<DayOfWeek, Duration>>, List<ZepRegularWorkingTimes>> {

    @Override
    public Map<Range<LocalDate>,Map<DayOfWeek, Duration>> map(List<ZepRegularWorkingTimes> zepRegularWorkingTimes) {

        Map<Range<LocalDate>,Map<DayOfWeek, Duration>> result = new HashMap<>();

        if(zepRegularWorkingTimes==null) {
            return result;
        }

        try{
            for (int i = 0; i < zepRegularWorkingTimes.size();i++) {

                ZepRegularWorkingTimes zepRegularWorkingTime = zepRegularWorkingTimes.get(i);

                Map<DayOfWeek, Duration> regularWorkingHours = createRegularWorkingHours(zepRegularWorkingTime);

                Range<LocalDate> dateRange = Range.of(getStartDate(zepRegularWorkingTime), getEndDate(zepRegularWorkingTimes,i));
                result.put(dateRange, regularWorkingHours);
            }
            return result;

        }catch (Exception e){
            throw new ZepServiceException("While trying to map ZepRegularWorkingTimes to Map<DayOfWeek, Duration>, an error occurred", e);
        }
    }

    private Map<DayOfWeek, Duration> createRegularWorkingHours(ZepRegularWorkingTimes zepRegularWorkingTime) {
        Map<DayOfWeek, Duration> regularWorkingHours = new HashMap<>();
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

    private LocalDate getStartDate(ZepRegularWorkingTimes zepRegularWorkingTime) {
        return Optional.ofNullable(zepRegularWorkingTime.startDate())
                .map(LocalDateTime::toLocalDate)
                .orElse(LocalDate.EPOCH);
    }

    private LocalDate getEndDate(List<ZepRegularWorkingTimes> zepRegularWorkingTimes, int index) {
        return (index + 1 < zepRegularWorkingTimes.size())
                ? zepRegularWorkingTimes.get(index + 1).startDate().toLocalDate().minusDays(1)
                : LocalDate.now();
    }
}
