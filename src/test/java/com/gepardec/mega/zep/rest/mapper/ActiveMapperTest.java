package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class ActiveMapperTest {
    @Inject
    ActiveMapper activeMapper;

    @Test
    public void map_whenPastStartDateFutureEndDate_isActive() {
        var employmentPeriods = createEmploymentPeriodListFromTodayOffset(
                new DateOffset(-10, 2)
        );

        boolean active = activeMapper.map(employmentPeriods);

        assertThat(active).isTrue();
    }
    @Test
    public void map_whenTodayStartEndDate_isActive() {
        var employmentPeriods = createEmploymentPeriodListFromTodayOffset(
                new DateOffset(0, 0)
        );

        boolean active = activeMapper.map(employmentPeriods);

        assertThat(active).isTrue();
    }

    @Test
    public void map_whenPastStartDateTodayEndDate_isActive() {
        var employmentPeriods = createEmploymentPeriodListFromTodayOffset(
                new DateOffset(-2, 0)
        );

        boolean active = activeMapper.map(employmentPeriods);

        assertThat(active).isTrue();
    }

    @Test
    public void map_whenTodayStartDateFutureEndDate_isActive() {
        var employmentPeriods = createEmploymentPeriodListFromTodayOffset(
                new DateOffset(0, 1)
        );

        boolean active = activeMapper.map(employmentPeriods);

        assertThat(active).isTrue();
    }

    @Test
    public void map_whenNullEndDate_isActive() {
        var employmentPeriods = List.of(
            createEmploymentPeriodWithNullEndDate(-2)
        );

        boolean active = activeMapper.map(employmentPeriods);

        assertThat(active).isTrue();
    }

    @Test
    public void map_whenMultipleInactiveDatesWithOneNullEndDate_isActive() {
        var employmentPeriods = createEmploymentPeriodListFromTodayOffset(
                new DateOffset(-100, -50),
                new DateOffset(-50, -25),
                new DateOffset(-25, -12),
                new DateOffset(-6, -3)
        );
        employmentPeriods.add(createEmploymentPeriodWithNullEndDate(-2));

        boolean active = activeMapper.map(employmentPeriods);

        assertThat(active).isTrue();
    }

    @Test
    public void map_whenMultipleInactiveDatesWithFutureEndDate_isActive() {
        var employmentPeriods = createEmploymentPeriodListFromTodayOffset(
                new DateOffset(-100, -50),
                new DateOffset(-50, -25),
                new DateOffset(-25, -12),
                new DateOffset(-6, 3)
        );

        boolean active = activeMapper.map(employmentPeriods);

        assertThat(active).isTrue();
    }

    @Test
    public void map_whenActiveDatePlusNullDate_isActive() {
        var employmentPeriods = createEmploymentPeriodListFromTodayOffset(
                new DateOffset(-100, 2)
        );
        employmentPeriods.add(createEmploymentPeriodWithNullDates());

        boolean active = activeMapper.map(employmentPeriods);

        assertThat(active).isTrue();
    }

    @Test
    public void map_whenMultipleActiveDates_isActive() {
        var employmentPeriods = createEmploymentPeriodListFromTodayOffset(
                new DateOffset(-2, 10),
                new DateOffset(-2, 10)
        );

        boolean active = activeMapper.map(employmentPeriods);

        assertThat(active).isTrue();
    }

    @Test
    public void map_whenPastStartEndDate_isInactive() {
        var employmentPeriods = createEmploymentPeriodListFromTodayOffset(
            new DateOffset(-10, -2)
        );

        boolean active = activeMapper.map(employmentPeriods);

        assertThat(active).isFalse();
    }

    @Test
    public void map_whenFutureStartEndDate_isInactive() {
        var employmentPeriods = createEmploymentPeriodListFromTodayOffset(
            new DateOffset(2, 2)
        );

        boolean active = activeMapper.map(employmentPeriods);

        assertThat(active).isFalse();
    }

    @Test
    public void map_whenEmptyList_isInactive() {
        List<ZepEmploymentPeriod> employmentPeriods = List.of();

        boolean active = activeMapper.map(employmentPeriods);

        assertThat(active).isFalse();
    }

    @Test
    public void map_whenNullStartDate_isInactive() {
        var employmentPeriods = List.of(
            createEmploymentPeriodWithNullStartDate(2)
        );

        boolean active = activeMapper.map(employmentPeriods);
        assertThat(active).isFalse();
    }

    @Test
    public void map_whenNullPeriod_isInactive() {
        var employmentPeriods = List.of(
            createEmploymentPeriodWithNullDates()
        );

        boolean active = activeMapper.map(employmentPeriods);
        assertThat(active).isFalse();
    }

    private ZepEmploymentPeriod createEmploymentPeriodWithNullEndDate(int offsetStart) {
        LocalDateTime now = LocalDateTime.now();
        return ZepEmploymentPeriod.builder()
                .startDate(now.plusDays(offsetStart))
                .endDate(null)
                .build();
    }

    private ZepEmploymentPeriod createEmploymentPeriodWithNullStartDate(int offsetEnd) {
        LocalDateTime now = LocalDateTime.now();
        return ZepEmploymentPeriod.builder()
                .startDate(null)
                .endDate(now.plusDays(offsetEnd))
                .build();
    }

    private ZepEmploymentPeriod createEmploymentPeriodWithNullDates() {
        return ZepEmploymentPeriod.builder().build();
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
