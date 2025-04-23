package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.rest.dto.ZepEmploymentPeriod;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class FirstDayCurrentPeriodMapperTest {
    @Inject
    FirstDayCurrentPeriodMapper firstDayCurrentPeriodMapper;


    @Test
    void map_whenNoEmploymentPeriods_thenReturnNull() {
        List<ZepEmploymentPeriod> employmentPeriods = List.of();

        var firstDay = firstDayCurrentPeriodMapper.map(employmentPeriods);

        assertNull(firstDay);
    }

    @Test
    void map_whenOnlyFutureStartDate_thenReturnNull() {
        List<ZepEmploymentPeriod> employmentPeriods = createEmploymentPeriodListFromTodayOffset(
                new DateOffset(1, 2)
        );

        var firstDay = firstDayCurrentPeriodMapper.map(employmentPeriods);

        assertNull(firstDay);
    }

    @Test
    void map_whenOnlyPastStartDate_thenReturnFirstDay() {
        List<ZepEmploymentPeriod> employmentPeriods = createEmploymentPeriodListFromTodayOffset(
                new DateOffset(-1, 2)
        );

        var firstDay = firstDayCurrentPeriodMapper.map(employmentPeriods);

        assertNotNull(firstDay);
    }

    @Test
    void map_whenOnlyPastStartDateAndNullEndDate_thenReturnFirstDay() {
        List<ZepEmploymentPeriod> employmentPeriods = createEmploymentPeriodListFromTodayOffset(
                new DateOffset(-1, 2)
        );
        employmentPeriods.add(createEmploymentPeriodWithNullEndDate(-1));

        var firstDay = firstDayCurrentPeriodMapper.map(employmentPeriods);

        assertNotNull(firstDay);
    }

    @Test
    void map_whenMultipleEmploymentPeriods_thenReturnFirstDayCurrentEmploymentPeriod() {
        List<ZepEmploymentPeriod> employmentPeriods = createEmploymentPeriodListFromTodayOffset(
                new DateOffset(-1, 2),
                new DateOffset(-2, 3)
        );

        var firstDay = firstDayCurrentPeriodMapper.map(employmentPeriods);

        assertNotNull(firstDay);
        assertEquals(LocalDate.from(employmentPeriods.stream().findFirst().get().startDate()), firstDay);
    }

    private ZepEmploymentPeriod createEmploymentPeriodWithNullEndDate(int offsetStart) {
        LocalDateTime now = LocalDateTime.now();
        return ZepEmploymentPeriod.builder()
                .startDate(now.plusDays(offsetStart))
                .endDate(null)
                .build();
    }

    private List<ZepEmploymentPeriod> createEmploymentPeriodListFromTodayOffset(DateOffset... dateOffsets) {
        return Stream.of(dateOffsets)
                .map(this::createEmploymentPeriodFromTodayOffset)
                .collect(Collectors.toList());
    }

    private ZepEmploymentPeriod createEmploymentPeriodFromTodayOffset(DateOffset dateOffset) {
        LocalDateTime now = LocalDateTime.now();
        return ZepEmploymentPeriod.builder()
                .startDate(now.plusDays(dateOffset.offsetStart()))
                .endDate(now.plusDays(dateOffset.offsetEnd()))
                .build();
    }

    private record DateOffset(int offsetStart, int offsetEnd) {
    }

}