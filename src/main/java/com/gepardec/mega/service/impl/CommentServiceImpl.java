package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.employee.EmployeeState;
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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@ApplicationScoped
public class CommentServiceImpl implements CommentService {

    @Inject
    CommentRepository commentRepository;

    @Inject
    CommentMapper commentMapper;

    @Inject
    MailSender mailSender;

    @Inject
    StepEntryService stepEntryService;

    @Override
    public List<Comment> findCommentsForEmployee(final String employeeEmail,
                                                 final YearMonth payrollMonth) {
        return commentRepository.findAllCommentsBetweenStartDateAndEndDateAndAllOpenCommentsBeforeStartDateForEmail(
                        payrollMonth.atDay(1),
                        payrollMonth.atEndOfMonth(),
                        employeeEmail
                )
                .stream()
                .map(commentMapper::mapToDomain)
                .toList();
    }

    @Override
    public int finish(final Comment comment) {
        var commentEntity = commentRepository.findById(comment.getId());
        sendMail(Mail.COMMENT_CLOSED, commentEntity);
        return commentRepository.setStatusDone(comment.getId());
    }

    @Override
    public FinishedAndTotalComments countFinishedAndTotalComments(final String employeeMail,
                                                                  final YearMonth payrollMonth) {
        Objects.requireNonNull(employeeMail, "'employeeMail' must not be null!");
        Objects.requireNonNull(payrollMonth, "'payrollMonth' must not be null!");

        var allComments =
                commentRepository.findAllCommentsBetweenStartDateAndEndDateAndAllOpenCommentsBeforeStartDateForEmail(
                        payrollMonth.atDay(1), payrollMonth.atEndOfMonth(), employeeMail
                );

        long finishedComments = allComments.stream()
                .filter(comment -> EmployeeState.DONE.equals(comment.getState()))
                .count();
        return FinishedAndTotalComments.builder()
                .finishedComments(finishedComments)
                .totalComments((long) allComments.size())
                .build();
    }

    @Override
    public Comment create(final Long stepId,
                          final SourceSystem sourceSystem,
                          final String employeeEmail,
                          final String message,
                          final String assigneeEmail,
                          final String project,
                          final YearMonth payrollMonth) {
        Objects.requireNonNull(employeeEmail, "'employeeEmail' must not be null");
        var stepEntry = StringUtils.isBlank(project)
                ? stepEntryService.findStepEntryForEmployeeAtStep(stepId, employeeEmail, assigneeEmail, payrollMonth)
                : stepEntryService.findStepEntryForEmployeeAndProjectAtStep(stepId, employeeEmail, assigneeEmail, project, payrollMonth);

        var comment = new com.gepardec.mega.db.entity.employee.Comment();
        comment.setMessage(message);
        comment.setStepEntry(stepEntry);
        comment.setSourceSystem(sourceSystem);

        commentRepository.save(comment);

        if (SourceSystem.MEGA.equals(comment.getSourceSystem())) {
            sendMail(Mail.COMMENT_CREATED, comment);
        }

        return commentMapper.mapToDomain(comment);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public boolean delete(final Long id) {
        var comment = commentRepository.findById(id);
        boolean deleted = commentRepository.deleteComment(id);

        if (deleted) {
            sendMail(Mail.COMMENT_DELETED, comment);
        }

        return deleted;
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Comment update(final Long id, final String message) {
        var comment = commentRepository.findById(id);
        if (comment == null) {
            throw new EntityNotFoundException("No entity found for id = %d".formatted(id));
        }

        comment.setMessage(message);

        commentRepository.update(comment);
        sendMail(Mail.COMMENT_MODIFIED, comment);

        return commentMapper.mapToDomain(comment);
    }

    private void sendMail(Mail mail, com.gepardec.mega.db.entity.employee.Comment comment) {
        var stepEntry = comment.getStepEntry();
        var creator = comment.getStepEntry().getAssignee().getFirstname();
        var recipient = Mail.COMMENT_CLOSED.equals(mail)
                ? stepEntry.getAssignee().getFirstname()
                : stepEntry.getOwner().getFirstname();
        var recipientEmail = Mail.COMMENT_CLOSED.equals(mail)
                ? stepEntry.getAssignee().getEmail()
                : stepEntry.getOwner().getEmail();
        Map<String, String> mailParameter = new HashMap<>();
        mailParameter.put(MailParameter.CREATOR, creator);
        mailParameter.put(MailParameter.RECIPIENT, recipient);
        mailParameter.put(MailParameter.COMMENT, comment.getMessage());

        mailSender.send(
                mail,
                recipientEmail,
                stepEntry.getOwner().getFirstname(),
                Locale.GERMAN,
                mailParameter,
                List.of(creator)
        );
    }
}
