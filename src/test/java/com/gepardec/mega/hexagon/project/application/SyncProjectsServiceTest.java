package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ZepProjectPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SyncProjectsServiceTest {

    private ZepProjectPort zepProjectPort;
    private ProjectRepository projectRepository;
    private SyncProjectsService service;

    @BeforeEach
    void setUp() {
        zepProjectPort = mock(ZepProjectPort.class);
        projectRepository = mock(ProjectRepository.class);
        service = new SyncProjectsService(zepProjectPort, projectRepository);
    }

    private ZepProjectProfile profile(int zepId, String name) {
        return new ZepProjectProfile(zepId, name, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
    }

    @Test
    void sync_createsNewProjectForUnknownZepId() {
        when(zepProjectPort.fetchAll()).thenReturn(List.of(profile(42, "Alpha")));
        when(projectRepository.findAll()).thenReturn(List.of());

        service.sync();

        verify(projectRepository).saveAll(argThat(projects -> {
            assertThat(projects).hasSize(1);
            assertThat(projects.getFirst().zepId()).isEqualTo(42);
            assertThat(projects.getFirst().name()).isEqualTo("Alpha");
            assertThat(projects.getFirst().id()).isNotNull();
            return true;
        }));
    }

    @Test
    void sync_updatesExistingProjectByZepId() {
        ProjectId existingId = ProjectId.generate();
        Project existing = Project.reconstitute(existingId, 42, "Old Name",
                LocalDate.of(2023, 1, 1), null, Set.of());

        when(zepProjectPort.fetchAll()).thenReturn(List.of(profile(42, "New Name")));
        when(projectRepository.findAll()).thenReturn(List.of(existing));

        service.sync();

        verify(projectRepository).saveAll(argThat(projects -> {
            assertThat(projects).hasSize(1);
            assertThat(projects.getFirst().id()).isEqualTo(existingId);
            assertThat(projects.getFirst().name()).isEqualTo("New Name");
            assertThat(projects.getFirst().startDate()).isEqualTo(LocalDate.of(2024, 1, 1));
            return true;
        }));
    }

    @Test
    void sync_preservesProjectIdOnUpdate() {
        ProjectId existingId = ProjectId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        Project existing = Project.reconstitute(existingId, 7, "X", LocalDate.now(), null, Set.of());

        when(zepProjectPort.fetchAll()).thenReturn(List.of(profile(7, "X Updated")));
        when(projectRepository.findAll()).thenReturn(List.of(existing));

        service.sync();

        verify(projectRepository).saveAll(argThat(projects -> {
            assertThat(projects.getFirst().id()).isEqualTo(existingId);
            return true;
        }));
    }

    @Test
    void sync_doesNotModifyLeads() {
        UUID leadId = UUID.randomUUID();
        Project existing = Project.reconstitute(ProjectId.generate(), 5, "Y",
                LocalDate.now(), null, Set.of(leadId));

        when(zepProjectPort.fetchAll()).thenReturn(List.of(profile(5, "Y Updated")));
        when(projectRepository.findAll()).thenReturn(List.of(existing));

        service.sync();

        verify(projectRepository).saveAll(argThat(projects -> {
            assertThat(projects.getFirst().leads()).containsExactly(leadId);
            return true;
        }));
    }

    @Test
    void sync_savesAllProjectsInOneBatch() {
        when(zepProjectPort.fetchAll()).thenReturn(List.of(profile(1, "A"), profile(2, "B"), profile(3, "C")));
        when(projectRepository.findAll()).thenReturn(List.of());

        service.sync();

        verify(projectRepository).saveAll(argThat(projects -> {
            assertThat(projects).hasSize(3);
            return true;
        }));
    }

    @Test
    void sync_handlesEmptyZepResponse() {
        when(zepProjectPort.fetchAll()).thenReturn(List.of());
        when(projectRepository.findAll()).thenReturn(List.of());

        service.sync();

        verify(projectRepository).saveAll(argThat(projects -> {
            assertThat(projects).isEmpty();
            return true;
        }));
    }
}
