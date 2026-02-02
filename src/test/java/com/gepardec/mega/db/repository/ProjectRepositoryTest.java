package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.project.ProjectEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@QuarkusTest
class ProjectRepositoryTest {

    @Inject
    ProjectRepository projectRepository;

    private ProjectEntity project;

    @BeforeEach
    void setUp() {
        project = new ProjectEntity();
        project.setName("LIW-Allgemein");
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now());

        projectRepository.persist(project);
    }

    @Test
    void givenFindById_whenFound_thenSuccess() {
        ProjectEntity p = projectRepository.findById(project.getId());

        assertAll(
                () -> assertThat(p).isNotNull(),
                () -> assertThat(p.getName()).isEqualTo(project.getName())
        );
    }
}
