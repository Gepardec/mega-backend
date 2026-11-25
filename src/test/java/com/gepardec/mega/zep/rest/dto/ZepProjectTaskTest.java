package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.helper.ResourceFileService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ZepProjectTaskTest {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ResourceFileService resourceFileService;

    @Test
    void deserialize_whenFullObservedTaskShape_thenOnlyIdNameAndProjectIdAreMapped() throws Exception {
        // Given
        String json = resourceFileService.getSingleFile("/projects/08_projectTask.json").orElseThrow();

        // When
        ZepResponse<List<ZepProjectTask>> response = objectMapper.readValue(json, new TypeReference<>() {
        });

        // Then
        assertThat(response.data()).singleElement().satisfies(task -> {
            assertThat(task.id()).isEqualTo(873);
            assertThat(task.name()).isEqualTo("Reisepauschale");
            assertThat(task.projectId()).isEqualTo(3);
        });
    }
}
