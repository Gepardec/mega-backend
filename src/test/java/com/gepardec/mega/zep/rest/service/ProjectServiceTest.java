package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.dto.ZepProjectTask;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ProjectServiceTest {

    @RestClient
    @InjectMock
    ZepProjectRestClient zepProjectRestClient;

    @Inject
    ProjectService projectService;

    @Test
    void getProjectTasks_whenTwoPages_thenBothPagesAreRead() {
        // Given
        when(zepProjectRestClient.getProjectTasks(7, "reisepauschale", 1))
                .thenReturn(Uni.createFrom().item(page(task(1, 7), "http://zep/next")));
        when(zepProjectRestClient.getProjectTasks(7, "reisepauschale", 2))
                .thenReturn(Uni.createFrom().item(page(task(2, 7), null)));

        // When
        List<ZepProjectTask> tasks = projectService.getProjectTasks(7, "reisepauschale");

        // Then
        verify(zepProjectRestClient).getProjectTasks(7, "reisepauschale", 1);
        verify(zepProjectRestClient).getProjectTasks(7, "reisepauschale", 2);
        assertThat(tasks).extracting(ZepProjectTask::id).containsExactly(1, 2);
    }

    @Test
    void getProjectTasks_whenZepAnswers404_thenEmptyList() {
        // Given
        when(zepProjectRestClient.getProjectTasks(99, "reisepauschale", 1))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException(404)));

        // When
        List<ZepProjectTask> tasks = projectService.getProjectTasks(99, "reisepauschale");

        // Then
        assertThat(tasks).isEmpty();
    }

    @Test
    void getProjectTasks_whenZepAnswersOtherError_thenErrorIsPropagated() {
        // Given
        when(zepProjectRestClient.getProjectTasks(7, "reisepauschale", 1))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException(500)));

        // When / Then
        assertThatThrownBy(() -> projectService.getProjectTasks(7, "reisepauschale"))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(500);
    }

    private static ZepProjectTask task(int id, int projectId) {
        return ZepProjectTask.builder().id(id).name("Reisepauschale").projectId(projectId).build();
    }

    private static ZepResponse<List<ZepProjectTask>> page(ZepProjectTask task, String next) {
        return new ZepResponse<>(List.of(task), new ZepResponse.Links(null, next));
    }
}
