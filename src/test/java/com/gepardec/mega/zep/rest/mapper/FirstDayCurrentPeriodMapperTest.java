package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.rest.dto.ZepEmploymentPeriod;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class FirstDayCurrentPeriodMapperTest {
    @Inject
    FirstDayCurrentPeriodMapper firstDayCurrentPeriodMapper;


    @Test
    void map_whenNoEmploymentPeriods_thenReturnNull() {
        List<ZepEmploymentPeriod> emptyEmploymentPeriodsList = List.of();

        var firstDay = firstDayCurrentPeriodMapper.map(emptyEmploymentPeriodsList);

        assertThat(firstDay).isNull();
    }

    @Test
    void map_whenOnlyFutureStartDate_thenReturnNull() {
        ZepEmploymentPeriod employmentPeriodInTheFuture = ZepEmploymentPeriod.builder()
                .startDate(now().plusDays(1))
                .endDate(now().plusDays(2))
                .build();

        List<ZepEmploymentPeriod> employmentPeriods = List.of(employmentPeriodInTheFuture);

        var firstDay = firstDayCurrentPeriodMapper.map(employmentPeriods);

        assertThat(firstDay).isNull();
    }

    @Test
    void map_whenPastStartDate_thenReturnFirstDay() {
        ZepEmploymentPeriod employmentPeriodWithStartDateInThePast = ZepEmploymentPeriod.builder()
                .startDate(now().minusDays(1))
                .endDate(now().minusDays(2))
                .build();
        List<ZepEmploymentPeriod> employmentPeriods = List.of(employmentPeriodWithStartDateInThePast);

        var firstDay = firstDayCurrentPeriodMapper.map(employmentPeriods);

        assertThat(firstDay).isNotNull();
    }

    @Test
    void map_whenOnlyPastStartDateAndNullEndDate_thenReturnFirstDay() {
        ZepEmploymentPeriod epWithPastStartDateAndNullEndDate = ZepEmploymentPeriod.builder()
                .startDate(now().minusDays(1))
                .build();

        List<ZepEmploymentPeriod> employmentPeriods = List.of(epWithPastStartDateAndNullEndDate);


        var firstDay = firstDayCurrentPeriodMapper.map(employmentPeriods);

        assertThat(firstDay).isNotNull();
    }

    @Test
    void map_whenMultipleEmploymentPeriods_thenReturnFirstDayCurrentEmploymentPeriod() {
        ZepEmploymentPeriod oldEmploymentPeriod = ZepEmploymentPeriod.builder()
                .startDate(now().minusDays(10))
                .endDate(now().minusDays(2))
                .build();
        ZepEmploymentPeriod currentEmploymentPeriod = ZepEmploymentPeriod.builder()
                .startDate(now().minusDays(1))
                .endDate(now().plusDays(20))
                .build();

        List<ZepEmploymentPeriod> employmentPeriods = List.of(oldEmploymentPeriod, currentEmploymentPeriod);

        var firstDay = firstDayCurrentPeriodMapper.map(employmentPeriods);

        assertThat(firstDay).isEqualTo(currentEmploymentPeriod.startDate().toLocalDate());
    }
}
