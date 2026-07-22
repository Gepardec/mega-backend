package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.shared.application.security.ForbiddenException;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

public class ProjectSettingsServiceTest {
    private ProjectRepository projectRepository;
    private ProjectSettingsService projectSettingsService;

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        projectSettingsService = new ProjectSettingsService(projectRepository);
    }

    @Test
    void setLeistungsnachweisEnabled_shouldSaveUpdatedProject_whenUserIsLead() {
        UserId leadId = UserId.of(UUID.randomUUID());
        Project project = new Project(ProjectId.generate(),1,"X", LocalDate.now(),null, true,true, Set.of(leadId));

        when(projectRepository.findAllByIds(Set.of(project.id()))).thenReturn(List.of(project));
        projectSettingsService.setLeistungsnachweisEnabled(project.id(), leadId, false);

        verify(projectRepository).saveAll(argThat(projects ->  {
            Project savedProject = projects.getFirst();
            return !savedProject.leistungsnachweisEnabled();
        }));

    }

    @Test
    void setLeistungsnachweisEnabled_shouldThrowForbidden_whenUserIsNotLead() {
        UserId leadId = UserId.of(UUID.randomUUID());
        UserId otherLeadId = UserId.of(UUID.randomUUID());
        Project project = new Project(ProjectId.generate(),2,"Y", LocalDate.now(),null, true,true, Set.of(otherLeadId));

        when(projectRepository.findAllByIds(Set.of(project.id()))).thenReturn(List.of(project));
        Throwable thrown = catchThrowable(() -> projectSettingsService.setLeistungsnachweisEnabled(project.id(), leadId, false));

        assertThat(thrown)
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("is not a lead");

        verify(projectRepository, never()).saveAll(anyList());
    }

    @Test
    void setLeistungsnachweisEnabled_shouldThrowIllegalArgument_whenProjectDoesNotExist() {
        UserId leadId = UserId.of(UUID.randomUUID());
        Project project = new Project(ProjectId.generate(),3,"Z", LocalDate.now(),null, true,true, Set.of(leadId));

        when(projectRepository.findAllByIds(Set.of(project.id()))).thenReturn(List.of());
        Throwable thrown = catchThrowable(() -> projectSettingsService.setLeistungsnachweisEnabled(project.id(), leadId, false));

        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Project not found:");

        verify(projectRepository, never()).saveAll(anyList());
    }

    @Test
    void getLeadProjects_shouldReturnProjectsForLead() {
        UserId leadId = UserId.of(UUID.randomUUID());
        List<Project> expectedProjects = List.of(
                new Project(ProjectId.generate(), 1, "Project A", LocalDate.now(), null, true, true, Set.of(leadId)),
                new Project(ProjectId.generate(), 2, "Project B", LocalDate.now(), null, false, true, Set.of(leadId))
        );

        when(projectRepository.findAllByLead(leadId)).thenReturn(expectedProjects);
        List<Project> result = projectSettingsService.getLeadProjects(leadId);


        assertThat(result).isEqualTo(expectedProjects);
        verify(projectRepository).findAllByLead(leadId);
    }
}
