package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.application.port.inbound.ProjectSyncResult;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ZepProjectPort;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
        return new ZepProjectProfile(zepId, name, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), false);
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
        Project existing = new Project(existingId, 42, "Old Name",
                LocalDate.of(2023, 1, 1), null, false, Set.of());

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
        ProjectId existingId = ProjectId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
        Project existing = new Project(existingId, 7, "X", LocalDate.now(), null, false, Set.of());

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
        UserId leadId = UserId.of(UUID.randomUUID());
        Project existing = new Project(ProjectId.generate(), 5, "Y",
                LocalDate.now(), null, false, Set.of(leadId));

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

        verify(projectRepository, never()).saveAll(anyList());
    }

    @Test
    void sync_result_countsCreatedProjects() {
        when(zepProjectPort.fetchAll()).thenReturn(List.of(profile(1, "A"), profile(2, "B")));
        when(projectRepository.findAll()).thenReturn(List.of());

        ProjectSyncResult result = service.sync();

        assertThat(result.created()).isEqualTo(2);
        assertThat(result.updated()).isZero();
        assertThat(result.unchanged()).isZero();
    }

    @Test
    void sync_result_countsUpdatedProjects() {
        Project existing1 = new Project(ProjectId.generate(), 1, "A", LocalDate.now(), null, false, Set.of());
        Project existing2 = new Project(ProjectId.generate(), 2, "B", LocalDate.now(), null, false, Set.of());

        when(zepProjectPort.fetchAll()).thenReturn(List.of(profile(1, "A Updated"), profile(2, "B Updated")));
        when(projectRepository.findAll()).thenReturn(List.of(existing1, existing2));

        ProjectSyncResult result = service.sync();

        assertThat(result.created()).isZero();
        assertThat(result.updated()).isEqualTo(2);
        assertThat(result.unchanged()).isZero();
    }

    @Test
    void sync_result_countsMixedOperations() {
        Project existing = new Project(ProjectId.generate(), 1, "A", LocalDate.now(), null, false, Set.of());

        when(zepProjectPort.fetchAll()).thenReturn(List.of(profile(1, "A Updated"), profile(2, "New")));
        when(projectRepository.findAll()).thenReturn(List.of(existing));

        ProjectSyncResult result = service.sync();

        assertThat(result.created()).isEqualTo(1);
        assertThat(result.updated()).isEqualTo(1);
        assertThat(result.unchanged()).isZero();
    }

    @Test
    void sync_doesNotCountOrPersistUnchangedProjects() {
        Project existing = new Project(
                ProjectId.generate(),
                1,
                "A",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                false,
                Set.of()
        );

        when(zepProjectPort.fetchAll()).thenReturn(List.of(profile(1, "A")));
        when(projectRepository.findAll()).thenReturn(List.of(existing));

        ProjectSyncResult result = service.sync();

        assertThat(result.created()).isZero();
        assertThat(result.updated()).isZero();
        assertThat(result.unchanged()).isEqualTo(1);
        verify(projectRepository, never()).saveAll(anyList());
    }
}
