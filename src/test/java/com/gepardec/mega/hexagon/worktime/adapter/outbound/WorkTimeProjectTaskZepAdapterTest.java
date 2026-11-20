package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.worktime.domain.model.ProjectTask;
import com.gepardec.mega.zep.rest.dto.ZepProjectTask;
import com.gepardec.mega.zep.rest.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkTimeProjectTaskZepAdapterTest {

    @Mock
    private ProjectService projectService;

    private WorkTimeProjectTaskZepAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WorkTimeProjectTaskZepAdapter(projectService, new WorkTimeProjectTaskZepMapperImpl());
    }

    @Test
    void fetchTasks_whenSeveralProjects_thenTasksOfAllAreMapped() {
        when(projectService.getProjectTasks(10, "reisepauschale")).thenReturn(List.of(zepTask(500, "Reisepauschale", 10)));
        when(projectService.getProjectTasks(20, "reisepauschale")).thenReturn(List.of(
                zepTask(600, "reisepauschale", 20), zepTask(601, "Reisepauschale Kunde", 20)));

        List<ProjectTask> tasks = adapter.fetchTasks(Set.of(10, 20));

        assertThat(tasks).containsExactlyInAnyOrder(
                new ProjectTask(10, 500, "Reisepauschale"),
                new ProjectTask(20, 600, "reisepauschale"),
                new ProjectTask(20, 601, "Reisepauschale Kunde"));
    }

    @Test
    void fetchTasks_whenProjectHasNoTasks_thenItContributesNothing() {
        when(projectService.getProjectTasks(10, "reisepauschale")).thenReturn(List.of());
        when(projectService.getProjectTasks(20, "reisepauschale")).thenReturn(List.of(zepTask(600, "Reisepauschale", 20)));

        assertThat(adapter.fetchTasks(Set.of(10, 20))).containsExactly(new ProjectTask(20, 600, "Reisepauschale"));
    }

    @Test
    void fetchTasks_whenNoProjectIds_thenNoZepCall() {
        assertThat(adapter.fetchTasks(Set.of())).isEmpty();

        verifyNoInteractions(projectService);
    }

    private static ZepProjectTask zepTask(int id, String name, int projectId) {
        return ZepProjectTask.builder().id(id).name(name).projectId(projectId).build();
    }
}
