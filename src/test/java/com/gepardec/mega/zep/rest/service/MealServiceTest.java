package com.gepardec.mega.zep.rest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.helper.ResourceFileService;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepMeal;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class MealServiceTest {

    @RestClient
    @InjectMock
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    MealService mealService;

    @Inject
    ResourceFileService resourceFileService;

    @Inject
    ObjectMapper objectMapper;

    @BeforeEach
    void init() throws Exception {
        when(zepEmployeeRestClient.getMealsByUsername("001-duser", 1))
                .thenReturn(Uni.createFrom().item(readPage("/meals/001-duser-page1.json")));
        when(zepEmployeeRestClient.getMealsByUsername("001-duser", 2))
                .thenReturn(Uni.createFrom().item(readPage("/meals/001-duser-page2.json")));
    }

    @Test
    void getZepMealsByEmployeeNameForMonth_whenTwoPages_thenBothPagesAreRead() {
        // When
        List<ZepMeal> meals = mealService.getZepMealsByEmployeeNameForMonth("001-duser", YearMonth.of(2026, 10));

        // Then
        verify(zepEmployeeRestClient, times(1)).getMealsByUsername("001-duser", 1);
        verify(zepEmployeeRestClient, times(1)).getMealsByUsername("001-duser", 2);
        assertThat(meals).extracting(ZepMeal::id).containsExactly(2, 3);
    }

    @Test
    void getZepMealsByEmployeeNameForMonth_whenEntriesOutsideMonth_thenTheyAreDropped() {
        // When
        List<ZepMeal> meals = mealService.getZepMealsByEmployeeNameForMonth("001-duser", YearMonth.of(2026, 10));

        // Then
        assertThat(meals).extracting(meal -> meal.date().toLocalDate().getMonthValue()).containsOnly(10);
    }

    private ZepResponse<List<ZepMeal>> readPage(String path) throws Exception {
        String json = resourceFileService.getSingleFile(path).orElseThrow();
        return objectMapper.readValue(json, new TypeReference<>() {
        });
    }
}
