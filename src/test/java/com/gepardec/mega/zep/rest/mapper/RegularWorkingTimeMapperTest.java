package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RegularWorkingTimeMapperTest {

    @InjectMocks
    RegularWorkingTimeMapper regularWorkingTimeMapper;

    @Test
    void mapZepRegularWorkingTimesToRegularWorkingHoursMap() {
        LocalDateTime date = LocalDateTime.of(2019, 1, 2, 8, 1, 32);

        ZepRegularWorkingTimes zepRegularWorkingTimes = ZepRegularWorkingTimes.builder()
                .startDate(date)
                .monday(8.0)
                .tuesday(8.0)
                .wednesday(8.0)
                .thursday(8.0)
                .friday(8.0)
                .saturday(0.0)
                .sunday(0.0)
                .build();

        var regularWorkingTime = regularWorkingTimeMapper.map(zepRegularWorkingTimes);

        Map<DayOfWeek, Duration> workingHours = regularWorkingTime.workingHours();
        assertThat(regularWorkingTime.start()).isEqualTo(date.toLocalDate());
        assertThat(workingHours)
                .containsEntry(DayOfWeek.MONDAY, Duration.ofHours(8))
                .containsEntry(DayOfWeek.TUESDAY, Duration.ofHours(8))
                .containsEntry(DayOfWeek.WEDNESDAY, Duration.ofHours(8))
                .containsEntry(DayOfWeek.THURSDAY, Duration.ofHours(8))
                .containsEntry(DayOfWeek.FRIDAY, Duration.ofHours(8))
                .containsEntry(DayOfWeek.SATURDAY, Duration.ofHours(0))
                .containsEntry(DayOfWeek.SUNDAY, Duration.ofHours(0));
    }

    @Test
    void mapZepRegularWorkingHoursToRegularWorkingHoursMap_NullMappedToZero() {
        LocalDateTime date = LocalDateTime.of(2019, 1, 2, 8, 1, 32);

        ZepRegularWorkingTimes zepRegularWorkingTimes = ZepRegularWorkingTimes.builder()
                .startDate(date)
                .monday(null)
                .tuesday(8.0)
                .wednesday(8.0)
                .thursday(8.0)
                .friday(8.0)
                .saturday(null)
                .build();

        var regularWorkingTime = regularWorkingTimeMapper.map(zepRegularWorkingTimes);

        Map<DayOfWeek, Duration> workingHours = regularWorkingTime.workingHours();
        assertThat(regularWorkingTime.start()).isEqualTo(date.toLocalDate());
        assertThat(workingHours).containsEntry(DayOfWeek.MONDAY, Duration.ofHours(0))
                .containsEntry(DayOfWeek.TUESDAY, Duration.ofHours(8))
                .containsEntry(DayOfWeek.WEDNESDAY, Duration.ofHours(8))
                .containsEntry(DayOfWeek.THURSDAY, Duration.ofHours(8))
                .containsEntry(DayOfWeek.FRIDAY, Duration.ofHours(8))
                .containsEntry(DayOfWeek.SATURDAY, Duration.ofHours(0))
                .containsEntry(DayOfWeek.SUNDAY, Duration.ofHours(0));
    }

    @Test
    void mapZepRegularWorkingHoursToRegularWorkingHoursMap_EmptyObjectMappedToZero() {
        ZepRegularWorkingTimes zepRegularWorkingTimes = ZepRegularWorkingTimes.builder().build();

        var regularWorkingTime = regularWorkingTimeMapper.map(zepRegularWorkingTimes);

        Map<DayOfWeek, Duration> workingHours = regularWorkingTime.workingHours();
        assertThat(regularWorkingTime.start()).isNull();
        assertThat(workingHours)
                .containsEntry(DayOfWeek.MONDAY, Duration.ofHours(0))
                .containsEntry(DayOfWeek.TUESDAY, Duration.ofHours(0))
                .containsEntry(DayOfWeek.WEDNESDAY, Duration.ofHours(0))
                .containsEntry(DayOfWeek.THURSDAY, Duration.ofHours(0))
                .containsEntry(DayOfWeek.FRIDAY, Duration.ofHours(0))
                .containsEntry(DayOfWeek.SATURDAY, Duration.ofHours(0))
                .containsEntry(DayOfWeek.SUNDAY, Duration.ofHours(0));
    }
}
