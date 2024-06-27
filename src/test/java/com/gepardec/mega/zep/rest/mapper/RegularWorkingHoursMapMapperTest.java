package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class RegularWorkingHoursMapMapperTest {



    @Test
    public void mapZepRegularWorkingTimesToRegularWorkingHoursMap() {
        RegularWorkingHoursMapMapper regularWorkingHoursMapMapper = new RegularWorkingHoursMapMapper();
        ZepRegularWorkingTimes zepRegularWorkingTimes = ZepRegularWorkingTimes.builder()
                .startDate(LocalDateTime.of(2019, 1, 2, 8, 1, 32))
                .monday(8.0)
                .tuesday(8.0)
                .wednesday(8.0)
                .thursday(8.0)
                .friday(8.0)
                .saturday(0.0)
                .sunday(0.0)
                .build();

        Map<DayOfWeek, Duration> regularWorkingHours = regularWorkingHoursMapMapper.map(zepRegularWorkingTimes);

        assertThat(regularWorkingHours).containsEntry(DayOfWeek.MONDAY, Duration.ofHours(8));
        assertThat(regularWorkingHours).containsEntry(DayOfWeek.TUESDAY, Duration.ofHours(8));
        assertThat(regularWorkingHours).containsEntry(DayOfWeek.WEDNESDAY, Duration.ofHours(8));
        assertThat(regularWorkingHours).containsEntry(DayOfWeek.THURSDAY, Duration.ofHours(8));
        assertThat(regularWorkingHours).containsEntry(DayOfWeek.FRIDAY, Duration.ofHours(8));
        assertThat(regularWorkingHours).containsEntry(DayOfWeek.SATURDAY, Duration.ofHours(0));
        assertThat(regularWorkingHours).containsEntry(DayOfWeek.SUNDAY, Duration.ofHours(0));

    }

    @Test
    public void mapZepRegularWorkingHoursToRegularWorkingHoursMap_NullMappedToZero() {
        RegularWorkingHoursMapMapper regularWorkingHoursMapMapper = new RegularWorkingHoursMapMapper();

        ZepRegularWorkingTimes zepRegularWorkingTimes = ZepRegularWorkingTimes.builder()
                .startDate(LocalDateTime.of(2019, 1, 2, 8, 1, 32))
                .monday(null)
                .tuesday(8.0)
                .wednesday(8.0)
                .thursday(8.0)
                .friday(8.0)
                .saturday(null)
                .build();

        Map<DayOfWeek, Duration> regularWorkingHours = regularWorkingHoursMapMapper.map(zepRegularWorkingTimes);

        assertThat(regularWorkingHours.get(DayOfWeek.MONDAY)).isEqualTo(Duration.ofHours(0));
        assertThat(regularWorkingHours.get(DayOfWeek.TUESDAY)).isEqualTo(Duration.ofHours(8));
        assertThat(regularWorkingHours.get(DayOfWeek.WEDNESDAY)).isEqualTo(Duration.ofHours(8));
        assertThat(regularWorkingHours.get(DayOfWeek.THURSDAY)).isEqualTo(Duration.ofHours(8));
        assertThat(regularWorkingHours.get(DayOfWeek.FRIDAY)).isEqualTo(Duration.ofHours(8));
        assertThat(regularWorkingHours.get(DayOfWeek.SATURDAY)).isEqualTo(Duration.ofHours(0));
        assertThat(regularWorkingHours.get(DayOfWeek.SUNDAY)).isEqualTo(Duration.ofHours(0));
    }

    @Test
    public void mapZepRegularWorkingHoursToRegularWorkingHoursMap_EmptyObjectMappedToZero() {
        RegularWorkingHoursMapMapper regularWorkingHoursMapMapper = new RegularWorkingHoursMapMapper();

        ZepRegularWorkingTimes zepRegularWorkingTimes = ZepRegularWorkingTimes.builder().build();

        Map<DayOfWeek, Duration> regularWorkingHours = regularWorkingHoursMapMapper.map(zepRegularWorkingTimes);

        assertThat(regularWorkingHours.get(DayOfWeek.MONDAY)).isEqualTo(Duration.ofHours(0));
        assertThat(regularWorkingHours.get(DayOfWeek.TUESDAY)).isEqualTo(Duration.ofHours(0));
        assertThat(regularWorkingHours.get(DayOfWeek.WEDNESDAY)).isEqualTo(Duration.ofHours(0));
        assertThat(regularWorkingHours.get(DayOfWeek.THURSDAY)).isEqualTo(Duration.ofHours(0));
        assertThat(regularWorkingHours.get(DayOfWeek.FRIDAY)).isEqualTo(Duration.ofHours(0));
        assertThat(regularWorkingHours.get(DayOfWeek.SATURDAY)).isEqualTo(Duration.ofHours(0));
        assertThat(regularWorkingHours.get(DayOfWeek.SUNDAY)).isEqualTo(Duration.ofHours(0));
    }

    @Test
    void map_whenZepRegularWorkingTimes_thenReturnEmptyMap() {
        RegularWorkingHoursMapMapper regularWorkingHoursMapMapper = new RegularWorkingHoursMapMapper();

        assertThat(regularWorkingHoursMapMapper.map(null)).isEmpty();
    }
}
