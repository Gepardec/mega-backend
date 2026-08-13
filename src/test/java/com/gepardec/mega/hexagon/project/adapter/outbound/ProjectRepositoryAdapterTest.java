package com.gepardec.mega.hexagon.project.adapter.outbound;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestTransaction
public class ProjectRepositoryAdapterTest {

    @Inject
    ProjectRepositoryAdapter projectRepositoryAdapter;

    @Test
    void saveAll_andFindAllByIds_shouldPersistAndLoadLeistungsnachweisEnabled() {
        ProjectId id = ProjectId.generate();
        Project project = new Project(id, 88, "Test", LocalDate.now(), null, true, false, Set.of());

        projectRepositoryAdapter.saveAll(List.of(project));

        List<Project> loaded = projectRepositoryAdapter.findAllByIds(Set.of(id));
        assertThat(loaded).hasSize(1);
        assertThat(loaded.getFirst().leistungsnachweisEnabled()).isFalse();
    }
}
