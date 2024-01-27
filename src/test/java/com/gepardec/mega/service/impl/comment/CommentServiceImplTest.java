package com.gepardec.mega.service.impl.comment;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.CommentRepository;
import com.gepardec.mega.domain.mapper.CommentMapper;
import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.FinishedAndTotalComments;
import com.gepardec.mega.domain.model.SourceSystem;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.notification.mail.Mail;
import com.gepardec.mega.notification.mail.MailParameter;
import com.gepardec.mega.notification.mail.MailSender;
import com.gepardec.mega.service.api.CommentService;
import com.gepardec.mega.service.api.StepEntryService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private CommentRepository commentRepository;

    @Test
    void findCommentsForEmployee_when1DbComment_thenMap1DomainComment() {
        when(commentRepository.findAllCommentsBetweenStartDateAndEndDateAndAllOpenCommentsBeforeStartDateForEmail(ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.any(LocalDate.class), ArgumentMatchers.anyString())).thenReturn(List.of(createComment(1L, EmployeeState.OPEN)));

        LocalDate fromDate = DateUtils.getFirstDayOfFollowingMonth(LocalDate.now().toString());
        LocalDate toDate = DateUtils.getLastDayOfFollowingMonth(LocalDate.now().toString());
        List<Comment> domainComments = commentService.findCommentsForEmployee(
                "max.mustermann@gpeardec.com", fromDate, toDate
        );
        assertThat(domainComments).hasSize(1);
        assertThat(domainComments.get(0).getId()).isEqualTo(1);
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
        assertThatThrownBy(() -> commentService.countFinishedAndTotalComments(null, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'employeeMail' must not be null!");
    }

    @Test
    void cntFinishedAndTotalCommentsForEmployee_whenFromDateIsNull_thenThrowsException() {
        assertThatThrownBy(() -> commentService.countFinishedAndTotalComments("max.mustermann@gpeardec.com", null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'from' date must not be null!");
    }

    @Test
    void cntFinishedAndTotalCommentsForEmployee_whenToDateIsNull_thenThrowsException() {
        assertThatThrownBy(() -> commentService.countFinishedAndTotalComments("max.mustermann@gpeardec.com", LocalDate.now(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'to' date must not be null!");
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

        LocalDate fromDate = DateUtils.getFirstDayOfFollowingMonth(LocalDate.now().toString());
        LocalDate toDate = DateUtils.getLastDayOfFollowingMonth(LocalDate.now().toString());
        FinishedAndTotalComments result = commentService.countFinishedAndTotalComments("max.mustermann@gpeardec.com", fromDate, toDate);
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

        LocalDate fromDate = DateUtils.getFirstDayOfFollowingMonth(LocalDate.now().toString());
        LocalDate toDate = DateUtils.getLastDayOfFollowingMonth(LocalDate.now().toString());
        FinishedAndTotalComments result = commentService.countFinishedAndTotalComments("max.mustermann@gpeardec.com", fromDate, toDate);
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

        LocalDate fromDate = DateUtils.getFirstDayOfFollowingMonth(LocalDate.now().toString());
        LocalDate toDate = DateUtils.getLastDayOfFollowingMonth(LocalDate.now().toString());
        FinishedAndTotalComments result = commentService.countFinishedAndTotalComments("max.mustermann@gpeardec.com", fromDate, toDate);
        assertThat(result).isNotNull();
        assertThat(result.getTotalComments()).isZero();
        assertThat(result.getFinishedComments()).isZero();
    }

    @Test
    void createNewCommentForEmployee_whenEmployeeIsNull_thenThrowsException() {
        assertThatThrownBy(() -> commentService.countFinishedAndTotalComments(null, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'employeeMail' must not be null!");
    }

    @Test
    void createNewCommentForEmployee_whenValid_thenReturnCreatedComment() {
        //GIVEN
        StepEntry stepEntry = createStepEntry(1L);
        when(stepEntryService.findStepEntryForEmployeeAtStep(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.any(String.class),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()
        )).thenReturn(createStepEntry(1L));

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((com.gepardec.mega.db.entity.employee.Comment) args[0]).setUpdatedDate(LocalDateTime.now());
            ((com.gepardec.mega.db.entity.employee.Comment) args[0]).setState(EmployeeState.OPEN);
            return args[0];
        }).when(commentRepository).save(ArgumentMatchers.any(com.gepardec.mega.db.entity.employee.Comment.class));

        doNothing().when(mailSender).send(
                ArgumentMatchers.any(Mail.class),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(Locale.class),
                ArgumentMatchers.anyMap(),
                ArgumentMatchers.anyList()
        );

        Employee employee = createEmployee();
        String newComment = "My new comment!";

        //WHEN
        Comment createdComment = commentService.create(
                2L, SourceSystem.MEGA, "max.mustermann@gpeardec.com", newComment, "", null, LocalDate.now().toString()
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
        com.gepardec.mega.db.entity.employee.Comment originalComment = createComment(1L, EmployeeState.DONE);
        when(commentRepository.findById(ArgumentMatchers.anyLong())).thenReturn(originalComment);
        when(commentRepository.update(ArgumentMatchers.any(com.gepardec.mega.db.entity.employee.Comment.class))).thenReturn(null);

        Comment updatedComment = commentService.update(1L, "Updated message");
        assertThat(updatedComment).isNotNull();
        assertThat(updatedComment.getMessage()).isEqualTo("Updated message");
    }

    private com.gepardec.mega.db.entity.employee.Comment createComment(Long id, EmployeeState employeeState) {
        com.gepardec.mega.db.entity.employee.Comment comment = new com.gepardec.mega.db.entity.employee.Comment();
        comment.setId(id);
        comment.setCreationDate(LocalDateTime.now());
        comment.setMessage("Reisezeiten eintragen!");
        comment.setState(employeeState);
        comment.setUpdatedDate(LocalDateTime.now());
        comment.setStepEntry(createStepEntry(1L));
        return comment;
    }

    private Employee createEmployee() {
        return Employee.builder()
                .userId("1")
                .email("max.mustermann@gpeardec.com")
                .releaseDate(LocalDate.now().toString())
                .firstname("Max")
                .build();
    }

    private StepEntry createStepEntry(Long id) {
        StepEntry stepEntry = new StepEntry();
        stepEntry.setId(id);
        stepEntry.setCreationDate(LocalDateTime.now());
        stepEntry.setDate(LocalDate.now());
        stepEntry.setProject("Liwest-EMS");
        stepEntry.setState(EmployeeState.IN_PROGRESS);
        stepEntry.setUpdatedDate(LocalDateTime.now());
        stepEntry.setAssignee(createUser());
        stepEntry.setOwner(createUser());
        return stepEntry;
    }

    private User createUser() {
        User user = new User();
        user.setEmail("max.mustermann@gpeardec.com");
        user.setFirstname("Max");
        return user;
    }
}
