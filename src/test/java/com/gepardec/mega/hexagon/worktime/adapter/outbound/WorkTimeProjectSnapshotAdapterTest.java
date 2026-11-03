package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkTimeProjectSnapshotAdapterTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WorkTimeProjectSnapshotMapper mapper;

    private WorkTimeProjectSnapshotAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WorkTimeProjectSnapshotAdapter(projectRepository, mapper);
    }

    @Test
    void findBillabilityByZepIds_shouldReturnBillabilityOfKnownProjects() {
        when(projectRepository.findByZepId(1)).thenReturn(Optional.of(project(1, true, null)));
        when(projectRepository.findByZepId(2)).thenReturn(Optional.of(project(2, false, null)));

        Map<Integer, Boolean> billability = adapter.findBillabilityByZepIds(Set.of(1, 2));

        assertThat(billability).containsExactlyInAnyOrderEntriesOf(Map.of(1, true, 2, false));
    }

    @Test
    void findBillabilityByZepIds_shouldOmitUnknownProjects() {
        when(projectRepository.findByZepId(1)).thenReturn(Optional.of(project(1, true, null)));
        when(projectRepository.findByZepId(99)).thenReturn(Optional.empty());

        Map<Integer, Boolean> billability = adapter.findBillabilityByZepIds(Set.of(1, 99));

        assertThat(billability).containsOnlyKeys(1);
    }

    @Test
    void findBillabilityByZepIds_shouldIncludeInactiveProjects() {
        when(projectRepository.findByZepId(3)).thenReturn(Optional.of(project(3, false, LocalDate.of(2020, 12, 31))));

        Map<Integer, Boolean> billability = adapter.findBillabilityByZepIds(Set.of(3));

        assertThat(billability).containsExactlyEntriesOf(Map.of(3, false));
    }

    @Test
    void findBillabilityByZepIds_shouldReturnEmptyMapForNoIds() {
        assertThat(adapter.findBillabilityByZepIds(Set.of())).isEmpty();
    }

    private static Project project(int zepId, boolean billable, LocalDate endDate) {
        return Instancio.of(Project.class)
                .set(field(Project::zepId), zepId)
                .set(field(Project::billable), billable)
                .set(field(Project::leistungsnachweisEnabled), false)
                .set(field(Project::startDate), LocalDate.of(2020, 1, 1))
                .set(field(Project::endDate), endDate)
                .create();
    }
}
