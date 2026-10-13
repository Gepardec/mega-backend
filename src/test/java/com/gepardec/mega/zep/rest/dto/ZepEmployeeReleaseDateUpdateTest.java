package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ZepEmployeeReleaseDateUpdateTest {

    @Inject
    ObjectMapper objectMapper;

    @Test
    void serialize_whenUsernameAndReleaseDate_thenJsonContainsOnlyUsernameAndApprovalDate() throws Exception {
        // Given
        ZepEmployeeReleaseDateUpdate update = new ZepEmployeeReleaseDateUpdate("007-jbond", LocalDate.of(2026, 9, 30));

        // When
        String json = objectMapper.writeValueAsString(update);

        // Then
        assertThat(json).isEqualTo("{\"username\":\"007-jbond\",\"approval_date\":\"2026-09-30\"}");
    }
}
