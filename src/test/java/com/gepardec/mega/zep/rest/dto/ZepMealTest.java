package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ZepMealTest {

    @Inject
    ObjectMapper objectMapper;

    @Test
    void deserialize_whenSampleResponse_thenDateAndFlagsAreMapped() throws Exception {
        // Given
        String json = """
                {
                  "id": 17,
                  "employee_id": "001-duser",
                  "date": "2026-10-22T00:00:00.000000Z",
                  "breakfast": true,
                  "lunch": true,
                  "dinner": true
                }
                """;

        // When
        ZepMeal meal = objectMapper.readValue(json, ZepMeal.class);

        // Then
        assertThat(meal.id()).isEqualTo(17);
        assertThat(meal.employeeId()).isEqualTo("001-duser");
        assertThat(meal.date().toLocalDate()).isEqualTo(OffsetDateTime.parse("2026-10-22T00:00:00Z").toLocalDate());
        assertThat(meal.breakfast()).isTrue();
        assertThat(meal.lunch()).isTrue();
        assertThat(meal.dinner()).isTrue();
    }

    @Test
    void deserialize_whenUnknownField_thenItIsIgnored() throws Exception {
        // Given
        String json = """
                {
                  "id": 18,
                  "employee_id": "001-duser",
                  "date": "2026-10-23T00:00:00.000000Z",
                  "breakfast": false,
                  "lunch": false,
                  "dinner": false,
                  "something_new": "value"
                }
                """;

        // When
        ZepMeal meal = objectMapper.readValue(json, ZepMeal.class);

        // Then
        assertThat(meal.date().toLocalDate().toString()).isEqualTo("2026-10-23");
        assertThat(meal.breakfast()).isFalse();
        assertThat(meal.lunch()).isFalse();
        assertThat(meal.dinner()).isFalse();
    }
}
