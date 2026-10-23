package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkTimeProjectSnapshotAdapterTest {
    @Mock ProjectRepository projectRepository;
    @Mock WorkTimeProjectSnapshotMapper mapper;
    @InjectMocks WorkTimeProjectSnapshotAdapter adapter;

    @Test
    void findBillableZepIds_returnsBillableProject() {
        when(projectRepository.findByZepId(10)).thenReturn(Optional.of(project(10, true)));

        assertThat(adapter.findBillableZepIds(Set.of(10))).containsExactly(10);
    }

    @Test
    void findBillableZepIds_dropsNonBillableProject() {
        when(projectRepository.findByZepId(10)).thenReturn(Optional.of(project(10, false)));

        assertThat(adapter.findBillableZepIds(Set.of(10))).isEmpty();
    }

    @Test
    void findBillableZepIds_dropsUnknownZepId() {
        when(projectRepository.findByZepId(10)).thenReturn(Optional.empty());

        assertThat(adapter.findBillableZepIds(Set.of(10))).isEmpty();
    }

    @Test
    void findBillableZepIds_keepsOnlyBillableOfSeveralProjects() {
        when(projectRepository.findByZepId(10)).thenReturn(Optional.of(project(10, true)));
        when(projectRepository.findByZepId(20)).thenReturn(Optional.of(project(20, false)));
        when(projectRepository.findByZepId(30)).thenReturn(Optional.empty());

        assertThat(adapter.findBillableZepIds(Set.of(10, 20, 30))).containsExactly(10);
    }

    @Test
    void findBillableZepIds_emptyInputYieldsEmptySetWithoutRepositoryCalls() {
        assertThat(adapter.findBillableZepIds(Set.of())).isEmpty();

        verifyNoInteractions(projectRepository);
    }

    private Project project(int zepId, boolean billable) {
        return Instancio.of(Project.class)
                .set(field(Project::zepId), zepId)
                .set(field(Project::billable), billable)
                .set(field(Project::leistungsnachweisEnabled), false)
                .create();
    }
}
