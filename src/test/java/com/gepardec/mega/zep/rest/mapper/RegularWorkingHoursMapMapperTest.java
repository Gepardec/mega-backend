package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.rest.entity.ZepRegularWorkingTimes;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class RegularWorkingHoursMapMapperTest {



    @Test
    public void mapZepRegularWorkingTimesToRegularWorkingHoursMap() {
        RegularWorkingHoursMapMapper regularWorkingHoursMapMapper = new RegularWorkingHoursMapMapper();
        ZepRegularWorkingTimes zepRegularWorkingTimes = ZepRegularWorkingTimes.builder()
                .id(1)
                .employee_id("001")
                .start_date(LocalDateTime.of(2019, 1, 2, 8, 1, 32))
                .monday(8.0)
                .tuesday(8.0)
                .wednesday(8.0)
                .thursday(8.0)
                .friday(8.0)
                .saturday(0.0)
                .sunday(0.0)
                .is_monthly(false)
                .monthly_hours(0.0)
                .max_hours_in_month(0.0)
                .max_hours_in_week(40.0)
                .build();

        Map<DayOfWeek, Duration> regularWorkingHours = regularWorkingHoursMapMapper.map(zepRegularWorkingTimes);

        assertThat(regularWorkingHours.get(DayOfWeek.MONDAY)).isEqualTo(Duration.ofHours(8));
        assertThat(regularWorkingHours.get(DayOfWeek.TUESDAY)).isEqualTo(Duration.ofHours(8));
        assertThat(regularWorkingHours.get(DayOfWeek.WEDNESDAY)).isEqualTo(Duration.ofHours(8));
        assertThat(regularWorkingHours.get(DayOfWeek.THURSDAY)).isEqualTo(Duration.ofHours(8));
        assertThat(regularWorkingHours.get(DayOfWeek.FRIDAY)).isEqualTo(Duration.ofHours(8));
        assertThat(regularWorkingHours.get(DayOfWeek.SATURDAY)).isEqualTo(Duration.ofHours(0));
        assertThat(regularWorkingHours.get(DayOfWeek.SUNDAY)).isEqualTo(Duration.ofHours(0));

    }

    @Test
    public void mapZepRegularWorkingHoursToRegularWorkingHoursMap_NullMappedToZero() {
        RegularWorkingHoursMapMapper regularWorkingHoursMapMapper = new RegularWorkingHoursMapMapper();

        ZepRegularWorkingTimes zepRegularWorkingTimes = ZepRegularWorkingTimes.builder()
                .id(1)
                .employee_id("001")
                .start_date(LocalDateTime.of(2019, 1, 2, 8, 1, 32))
                .monday(null)
                .tuesday(8.0)
                .wednesday(8.0)
                .thursday(8.0)
                .friday(8.0)
                .saturday(null)
                //Sunday missing -> null
                .is_monthly(false)
                .monthly_hours(0.0)
                .max_hours_in_month(0.0)
                .max_hours_in_week(40.0)
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
}
