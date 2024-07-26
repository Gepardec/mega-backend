package com.gepardec.mega.service.impl.projectcomment;

import com.gepardec.mega.db.entity.project.Project;
import com.gepardec.mega.db.entity.project.ProjectComment;
import com.gepardec.mega.db.repository.ProjectCommentRepository;
import com.gepardec.mega.db.repository.ProjectRepository;
import com.gepardec.mega.rest.mapper.ProjectCommentMapper;
import com.gepardec.mega.rest.model.ProjectCommentDto;
import com.gepardec.mega.service.api.ProjectCommentService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ProjectCommentServiceImplTest {

    @InjectMock
    ProjectCommentRepository projectCommentRepository;

    @InjectMock
    ProjectRepository projectRepository;

    @InjectMock
    ProjectCommentMapper projectCommentMapper;

    @Inject
    ProjectCommentService projectCommentService;

    private ProjectComment projectComment;
    private ProjectCommentDto projectCommentDto;
    private Project project;

    @BeforeEach
    void setUp() {
        projectCommentDto = ProjectCommentDto.builder()
                .comment("New comment")
                .date(LocalDate.of(2023, 10, 1))
                .projectName("Test Project")
                .build();

        projectComment = new ProjectComment();
        projectComment.setId(1L);
        projectComment.setComment("Old comment");
        projectComment.setDate(LocalDate.of(2023, 10, 1));

        project = new Project();
        project.setName("Test Project");
    }

    @Test
    void findForProjectNameInRange_shouldReturnListOfCommentsInRange() {
        when(projectCommentRepository.findByProjectNameAndDateBetween(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(projectComment));


        when(projectCommentMapper.mapToDto(projectComment))
                .thenReturn(projectCommentDto);

        List<ProjectCommentDto> result = projectCommentService.findForProjectNameInRange("Test Project",
                LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 31));

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(projectCommentDto);
    }

    @Test
    void findForProjectNameWithCurrentYearMonth_shouldReturnCommentForCurrentYearMonth() {
        when(projectCommentRepository.findByProjectNameAndDateBetween(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(projectComment));
        when(projectCommentMapper.mapToDto(projectComment))
                .thenReturn(projectCommentDto);

        ProjectCommentDto result = projectCommentService.findForProjectNameWithCurrentYearMonth("Test Project", "2023-10-01");

        assertThat(result).isEqualTo(projectCommentDto);
    }

    @Test
    void findForProjectNameWithCurrentYearMonth_whenNoComment_shouldReturnNull() {
        when(projectCommentRepository.findByProjectNameAndDateBetween(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        ProjectCommentDto result = projectCommentService.findForProjectNameWithCurrentYearMonth("Test Project", "2023-10-01");

        assertThat(result).isNull();
    }

    @Test
    void create_whenNoExistingComment_savesNewComment() {
        when(projectRepository.findByName(anyString()))
                .thenReturn(project);
        when(projectCommentRepository.findByProjectNameWithDate(anyString(), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(projectCommentMapper.mapToDto(any(ProjectComment.class)))
                .thenReturn(projectCommentDto);

        ProjectCommentDto result = projectCommentService.create(projectCommentDto);

        verify(projectCommentRepository, times(1)).save(any(ProjectComment.class));
        verify(projectCommentRepository, never()).update(any(ProjectComment.class));
        assertThat(result).isEqualTo(projectCommentDto);
    }

    @Test
    void create_whenExistingComment_updatesExistingComment() {
        when(projectRepository.findByName(anyString()))
                .thenReturn(project);
        when(projectCommentRepository.findByProjectNameWithDate(anyString(), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(projectComment));
        when(projectCommentMapper.mapToDto(any(ProjectComment.class)))
                .thenReturn(projectCommentDto);

        ProjectCommentDto result = projectCommentService.create(projectCommentDto);

        verify(projectCommentRepository, never()).save(any(ProjectComment.class));
        verify(projectCommentRepository, times(1)).update(any(ProjectComment.class));
        assertThat(result).isEqualTo(projectCommentDto);
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenCommentNotFound() {
        when(projectCommentRepository.findById(anyLong()))
                .thenReturn(null);

        assertThatThrownBy(() -> projectCommentService.update(1L, "Updated comment"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_shouldUpdateCommentIfCommentExists() {
        when(projectCommentRepository.findById(anyLong()))
                .thenReturn(projectComment);

        when(projectCommentRepository.update(any(ProjectComment.class)))
                .thenReturn(true);

        boolean result = projectCommentService.update(1L, "Updated comment");

        verify(projectCommentRepository, times(1)).findById(anyLong());
        verify(projectCommentRepository, times(1)).update(any(ProjectComment.class));
        assertThat(result).isTrue();
        assertThat(projectComment.getComment()).isEqualTo("Updated comment");
    }
}
