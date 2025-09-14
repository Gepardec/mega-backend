package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.project.Project;
import com.gepardec.mega.db.entity.project.ProjectComment;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestTransaction
class ProjectCommentRepositoryTest {

    private static final String COMMENT = "comment";
    private static final String NEW_COMMENT = "new comment";

    @Inject
    ProjectCommentRepository projectCommentRepository;

    @Inject
    ProjectRepository projectRepository;

    private ProjectComment projectComment;

    @BeforeEach
    void setUp() {
        projectComment = initializeProjectCommentObject();
    }

    @Test
    void findByProjectNameAndDateBetween() {
        projectRepository.persist(projectComment.getProject());
        projectCommentRepository.save(projectComment);

        List<ProjectComment> projectComments = projectCommentRepository.findByProjectNameAndDateBetween(
                projectComment.getProject().getName(),
                projectComment.getDate().minusDays(2),
                projectComment.getDate().plusDays(2)
        );

        assertThat(projectComments).isNotEmpty()
                .first()
                .extracting(ProjectComment::getDate)
                .isEqualTo(projectComment.getDate());
    }

    @Test
    void findByProjectNameWithDate() {
        projectRepository.persist(projectComment.getProject());
        projectCommentRepository.save(projectComment);

        List<ProjectComment> projectComments = projectCommentRepository.findByProjectNameWithDate(
                projectComment.getProject().getName(),
                projectComment.getDate()
        );

        assertThat(projectComments).isNotEmpty()
                .first()
                .extracting(ProjectComment::getDate)
                .isEqualTo(projectComment.getDate());
    }

    @Test
    void update() {
        projectRepository.persist(projectComment.getProject());
        projectCommentRepository.save(projectComment);

        List<ProjectComment> projectComments = projectCommentRepository.findByProjectNameAndDateBetween(
                projectComment.getProject().getName(),
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(2)
        );
        projectComments.getFirst().setComment(NEW_COMMENT);
        projectCommentRepository.update(projectComments.getFirst());

        List<ProjectComment> newProjectComments = projectCommentRepository.findByProjectNameAndDateBetween(
                projectComment.getProject().getName(),
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(2)
        );

        assertThat(newProjectComments).isNotEmpty()
                .first()
                .extracting(ProjectComment::getComment)
                .isEqualTo(NEW_COMMENT);
    }

    @Test
    void save() {
        projectRepository.persist(projectComment.getProject());
        projectCommentRepository.save(projectComment);

        List<ProjectComment> projectComments = projectCommentRepository.findByProjectNameAndDateBetween(
                projectComment.getProject().getName(),
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(2)
        );

        assertThat(projectComments).isNotEmpty();
    }

    private Project initializeProjectObject() {
        Project initProject = new Project();
        initProject.setName("LIW-Allgemein");
        initProject.setStartDate(LocalDate.now());
        initProject.setEndDate(LocalDate.now());

        return initProject;
    }

    private ProjectComment initializeProjectCommentObject() {
        ProjectComment newProjectComment = new ProjectComment();
        newProjectComment.setComment(COMMENT);
        newProjectComment.setProject(initializeProjectObject());
        newProjectComment.setCreationDate(LocalDateTime.now());
        newProjectComment.setUpdatedDate(LocalDateTime.now());
        newProjectComment.setDate(LocalDate.now());

        return newProjectComment;
    }
}
