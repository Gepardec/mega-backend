package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetProjectSettingsServiceTest {

    private static final UserId LEAD_ID = UserId.of(UUID.randomUUID());
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-15T00:00:00Z"), ZoneOffset.UTC);

    private ProjectRepository projectRepository;
    private GetProjectSettingsService service;

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        service = new GetProjectSettingsService(projectRepository, CLOCK);
    }

    private Project billableProject(LocalDate startDate, LocalDate endDate, boolean flag) {
        return new Project(ProjectId.generate(), 1, "X", startDate, endDate, true, flag, Set.of(LEAD_ID));
    }

    @Test
    void getLeadProjects_excludesNonBillableProjects() {
        Project nonBillable = new Project(ProjectId.generate(), 1, "X", LocalDate.of(2024, 1, 1), null, false, false, Set.of(LEAD_ID));
        when(projectRepository.findAllByLead(LEAD_ID)).thenReturn(List.of(nonBillable));

        List<Project> result = service.getLeadProjects(LEAD_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void getLeadProjects_excludesEndedProjects() {
        Project ended = billableProject(LocalDate.of(2024, 1, 1), LocalDate.of(2026, 8, 31), true);
        when(projectRepository.findAllByLead(LEAD_ID)).thenReturn(List.of(ended));

        List<Project> result = service.getLeadProjects(LEAD_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void getLeadProjects_includesBillableProjectWithFlagDisabled() {
        Project project = billableProject(LocalDate.of(2024, 1, 1), null, false);
        when(projectRepository.findAllByLead(LEAD_ID)).thenReturn(List.of(project));

        List<Project> result = service.getLeadProjects(LEAD_ID);

        assertThat(result).containsExactly(project);
    }

    @Test
    void getLeadProjects_includesFutureProject() {
        Project futureProject = billableProject(LocalDate.of(2026, 11, 1), null, true);
        when(projectRepository.findAllByLead(LEAD_ID)).thenReturn(List.of(futureProject));

        List<Project> result = service.getLeadProjects(LEAD_ID);

        assertThat(result).containsExactly(futureProject);
    }
}
