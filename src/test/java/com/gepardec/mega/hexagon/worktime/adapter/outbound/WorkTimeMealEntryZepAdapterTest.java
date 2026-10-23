package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.zep.rest.dto.ZepMeal;
import com.gepardec.mega.zep.rest.service.MealService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkTimeMealEntryZepAdapterTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 10);

    @Mock
    private MealService mealService;

    private WorkTimeMealEntryZepAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WorkTimeMealEntryZepAdapter(mealService);
    }

    @Test
    void fetchMealEntryDates_shouldMapEachEntryToItsDate() {
        when(mealService.getZepMealsByEmployeeNameForMonth("ada", MONTH)).thenReturn(List.of(
                meal("2026-10-12T00:00:00Z", true, true, true),
                meal("2026-10-14T00:00:00Z", true, false, false)
        ));

        Set<LocalDate> dates = adapter.fetchMealEntryDates(ZepUsername.of("ada"), MONTH);

        assertThat(dates).containsExactlyInAnyOrder(LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 14));
    }

    @Test
    void fetchMealEntryDates_whenAllFlagsFalse_shouldStillYieldTheDate() {
        when(mealService.getZepMealsByEmployeeNameForMonth("ada", MONTH)).thenReturn(List.of(
                meal("2026-10-13T00:00:00Z", false, false, false)
        ));

        Set<LocalDate> dates = adapter.fetchMealEntryDates(ZepUsername.of("ada"), MONTH);

        assertThat(dates).containsExactly(LocalDate.of(2026, 10, 13));
    }

    @Test
    void fetchMealEntryDates_whenDateMissing_shouldSkipTheEntry() {
        when(mealService.getZepMealsByEmployeeNameForMonth("ada", MONTH)).thenReturn(List.of(
                ZepMeal.builder().breakfast(true).build(),
                meal("2026-10-12T00:00:00Z", true, false, false)
        ));

        Set<LocalDate> dates = adapter.fetchMealEntryDates(ZepUsername.of("ada"), MONTH);

        assertThat(dates).containsExactly(LocalDate.of(2026, 10, 12));
    }

    private ZepMeal meal(String date, boolean breakfast, boolean lunch, boolean dinner) {
        return ZepMeal.builder()
                .date(OffsetDateTime.parse(date))
                .breakfast(breakfast)
                .lunch(lunch)
                .dinner(dinner)
                .build();
    }
}
