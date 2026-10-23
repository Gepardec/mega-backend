package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZepAttendanceTest {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void deserialize_readsNote() throws IOException {
        try (InputStream json = getClass().getResourceAsStream("/zep/rest/testresponses/attendances/002-tuser/ZepAttendanceList.json")) {
            List<ZepAttendance> attendances = objectMapper.readValue(
                    objectMapper.readTree(json).get("data").traverse(),
                    new TypeReference<>() {
                    });

            assertThat(attendances.getFirst().note()).isEqualTo("Jenkins aufräumen weil Speicherplatz ausgegangen ist");
        }
    }

    @Test
    void deserialize_readsStartAndDestination() throws IOException {
        String json = """
                {"id": 1, "start": "Wien Hbf", "destination": "Linz"}
                """;

        ZepAttendance attendance = objectMapper.readValue(json, ZepAttendance.class);

        assertThat(attendance.start()).isEqualTo("Wien Hbf");
        assertThat(attendance.destination()).isEqualTo("Linz");
    }

    @Test
    void deserialize_whenStartAndDestinationAbsent_thenNull() throws IOException {
        ZepAttendance attendance = objectMapper.readValue("{\"id\": 1}", ZepAttendance.class);

        assertThat(attendance.start()).isNull();
        assertThat(attendance.destination()).isNull();
    }
}
