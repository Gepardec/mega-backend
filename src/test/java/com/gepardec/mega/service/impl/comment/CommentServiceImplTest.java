package com.gepardec.mega.service.impl.comment;

import com.gepardec.mega.db.entity.employee.CommentEntity;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntryEntity;
import com.gepardec.mega.db.entity.employee.UserEntity;
import com.gepardec.mega.db.repository.CommentRepository;
import com.gepardec.mega.domain.mapper.CommentMapper;
import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.domain.model.FinishedAndTotalComments;
import com.gepardec.mega.domain.model.SourceSystem;
import com.gepardec.mega.notification.mail.Mail;
import com.gepardec.mega.notification.mail.MailParameter;
import com.gepardec.mega.notification.mail.MailSender;
import com.gepardec.mega.service.api.CommentService;
import com.gepardec.mega.service.api.StepEntryService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class CommentServiceImplTest {

    @Inject
    CommentService commentService;

    @Inject
    CommentMapper commentMapper;

    @InjectMock
    StepEntryService stepEntryService;

    @InjectMock
    MailSender mailSender;

    @InjectMock
    CommentRepository commentRepository;

    @Test
    void findCommentsForEmployee_when1DbComment_thenMap1DomainComment() {
        when(commentRepository.findAllCommentsBetweenStartDateAndEndDateAndAllOpenCommentsBeforeStartDateForEmail(ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.any(LocalDate.class), ArgumentMatchers.anyString())).thenReturn(List.of(createComment(1L, EmployeeState.OPEN)));

        List<Comment> domainComments = commentService.findCommentsForEmployee(
                "max.mustermann@gpeardec.com", YearMonth.now().plusMonths(1)
        );
        assertThat(domainComments).hasSize(1);
        assertThat(domainComments.getFirst().getId()).isEqualTo(1);
    }

    @Test
    void finish_whenNoneUpdated_then0() {
        when(commentRepository.findById(ArgumentMatchers.anyLong())).thenReturn(createComment(1L, EmployeeState.IN_PROGRESS));
        when(commentRepository.setStatusDone(ArgumentMatchers.anyLong())).thenReturn(0);

        int updatedCount = commentService.finish(commentMapper.mapToDomain(createComment(1L, EmployeeState.IN_PROGRESS)));
        assertThat(updatedCount).isZero();
    }

    @Test
    void cntFinishedAndTotalCommentsForEmployee_whenEmployeeIsNull_thenThrowsException() {
        assertThatThrownBy(() -> commentService.countFinishedAndTotalComments(null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'employeeMail' must not be null!");
    }

    @Test
    void cntFinishedAndTotalCommentsForEmployee_whenFromDateIsNull_thenThrowsException() {
        assertThatThrownBy(() -> commentService.countFinishedAndTotalComments("max.mustermann@gpeardec.com", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'payrollMonth' must not be null!");
    }

    @Test
    void cntFinishedAndTotalCommentsForEmployee_whenToDateIsNull_thenThrowsException() {
        assertThatThrownBy(() -> commentService.countFinishedAndTotalComments("max.mustermann@gpeardec.com", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'payrollMonth' must not be null!");
    }

    @Test
    void cntFinishedAndTotalCommentsForEmployee_whenValid_thenReturnsCnt() {
        when(commentRepository.findAllCommentsBetweenStartDateAndEndDateAndAllOpenCommentsBeforeStartDateForEmail(
                ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.anyString()
        )).thenReturn(List.of(
                createComment(1L, EmployeeState.IN_PROGRESS),
                createComment(2L, EmployeeState.DONE),
                createComment(3L, EmployeeState.OPEN)
        ));

        FinishedAndTotalComments result = commentService.countFinishedAndTotalComments("max.mustermann@gpeardec.com", YearMonth.now().plusMonths(1));
        assertThat(result).isNotNull();
        assertThat(result.getTotalComments()).isEqualTo(3L);
        assertThat(result.getFinishedComments()).isEqualTo(1L);
    }

    @Test
    void cntFinishedAndTotalCommentsForEmployee_whenNoFinishedComments_thenReturnsCnt() {
        when(commentRepository.findAllCommentsBetweenStartDateAndEndDateAndAllOpenCommentsBeforeStartDateForEmail(
                ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.anyString()
        )).thenReturn(List.of(
                createComment(1L, EmployeeState.IN_PROGRESS),
                createComment(2L, EmployeeState.OPEN),
                createComment(3L, EmployeeState.OPEN)
        ));

        FinishedAndTotalComments result = commentService.countFinishedAndTotalComments("max.mustermann@gpeardec.com", YearMonth.now().plusMonths(1));
        assertThat(result).isNotNull();
        assertThat(result.getTotalComments()).isEqualTo(3L);
        assertThat(result.getFinishedComments()).isZero();
    }

    @Test
    void cntFinishedAndTotalCommentsForEmployee_whenNoComments_thenReturnsCnt() {
        when(commentRepository.findAllCommentsBetweenStartDateAndEndDateAndAllOpenCommentsBeforeStartDateForEmail(
                ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.anyString()
        )).thenReturn(List.of());

        FinishedAndTotalComments result = commentService.countFinishedAndTotalComments("max.mustermann@gpeardec.com", YearMonth.now().plusMonths(1));
        assertThat(result).isNotNull();
        assertThat(result.getTotalComments()).isZero();
        assertThat(result.getFinishedComments()).isZero();
    }

    @Test
    void createNewCommentForEmployee_whenEmployeeIsNull_thenThrowsException() {
        assertThatThrownBy(() -> commentService.countFinishedAndTotalComments(null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'employeeMail' must not be null!");
    }

    @Test
    void createNewCommentForEmployee_whenValid_thenReturnCreatedComment() {
        //GIVEN
        StepEntryEntity stepEntry = createStepEntry();
        when(stepEntryService.findStepEntryForEmployeeAtStep(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.any(String.class),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(YearMonth.class)
        )).thenReturn(createStepEntry());

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((CommentEntity) args[0]).setUpdatedDate(LocalDateTime.now());
            ((CommentEntity) args[0]).setState(EmployeeState.OPEN);
            return args[0];
        }).when(commentRepository).save(ArgumentMatchers.any(CommentEntity.class));

        doNothing().when(mailSender).send(
                ArgumentMatchers.any(Mail.class),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(Locale.class),
                ArgumentMatchers.anyMap(),
                ArgumentMatchers.anyList()
        );

        String newComment = "My new comment!";

        //WHEN
        Comment createdComment = commentService.create(
                2L, SourceSystem.MEGA, "max.mustermann@gpeardec.com", newComment, "", null, YearMonth.now()
        );

        //THEN
        String creator = stepEntry.getAssignee().getFirstname();
        String recipient = stepEntry.getOwner().getFirstname();
        Map<String, String> expectedMailParameter = Map.of(
                MailParameter.CREATOR, creator,
                MailParameter.RECIPIENT, recipient,
                MailParameter.COMMENT, newComment
        );

        verify(mailSender, times(1)).send(
                Mail.COMMENT_CREATED, "max.mustermann@gpeardec.com", recipient, Locale.GERMAN, expectedMailParameter, List.of(creator)
        );

        assertThat(createdComment).isNotNull();
        assertThat(createdComment.getMessage()).isEqualTo("My new comment!");
        assertThat(createdComment.getAuthorEmail()).isEqualTo(stepEntry.getAssignee().getEmail());
        assertThat(createdComment.getState()).isEqualTo(EmployeeState.OPEN);
    }

    @Test
    void updateComment_whenEntityNotFound_thenThrowsException() {
        when(commentRepository.findById(ArgumentMatchers.anyLong())).thenReturn(null);

        assertThatThrownBy(() -> commentService.update(1L, "My message!"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("No entity found for id = 1");
    }

    @Test
    void updateComment_whenValid_thenReturnUpdatedComment() {
        CommentEntity originalComment = createComment(1L, EmployeeState.DONE);
        when(commentRepository.findById(ArgumentMatchers.anyLong())).thenReturn(originalComment);
        when(commentRepository.update(ArgumentMatchers.any(CommentEntity.class))).thenReturn(null);

        Comment updatedComment = commentService.update(1L, "Updated message");
        assertThat(updatedComment).isNotNull();
        assertThat(updatedComment.getMessage()).isEqualTo("Updated message");
    }

    @Test
    void deleteComment_whenSuccess_thenDeleteAndSendMail() {
        Long commentId = 1L;
        CommentEntity commentEntity = createComment(commentId, EmployeeState.IN_PROGRESS);
        when(commentRepository.findById(commentId)).thenReturn(commentEntity);
        when(commentRepository.deleteComment(commentId)).thenReturn(true);

        doNothing().when(mailSender).send(
                ArgumentMatchers.any(Mail.class),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(Locale.class),
                ArgumentMatchers.anyMap(),
                ArgumentMatchers.anyList()
        );

        boolean deleted = commentService.delete(commentId);

        assertThat(deleted).isTrue();
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).deleteComment(commentId);
    }

    @Test
    void deleteComment_whenNoSuccess_thenReturnFalse() {
        Long commentId = 1L;
        CommentEntity commentEntity = createComment(commentId, EmployeeState.IN_PROGRESS);
        when(commentRepository.findById(commentId)).thenReturn(commentEntity);
        when(commentRepository.deleteComment(commentId)).thenReturn(false);

        boolean deleted = commentService.delete(commentId);

        assertThat(deleted).isFalse();
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).deleteComment(commentId);
    }

    private CommentEntity createComment(Long id, EmployeeState employeeState) {
        CommentEntity comment = new CommentEntity();
        comment.setId(id);
        comment.setCreationDate(LocalDateTime.now());
        comment.setMessage("Reisezeiten eintragen!");
        comment.setState(employeeState);
        comment.setUpdatedDate(LocalDateTime.now());
        comment.setStepEntry(createStepEntry());
        return comment;
    }

    private StepEntryEntity createStepEntry() {
        StepEntryEntity stepEntry = new StepEntryEntity();
        stepEntry.setId(1L);
        stepEntry.setCreationDate(LocalDateTime.now());
        stepEntry.setDate(LocalDate.now());
        stepEntry.setProject("Liwest-EMS");
        stepEntry.setState(EmployeeState.IN_PROGRESS);
        stepEntry.setUpdatedDate(LocalDateTime.now());
        stepEntry.setAssignee(createUser());
        stepEntry.setOwner(createUser());
        return stepEntry;
    }

    private UserEntity createUser() {
        UserEntity user = new UserEntity();
        user.setEmail("max.mustermann@gpeardec.com");
        user.setFirstname("Max");
        return user;
    }
}
