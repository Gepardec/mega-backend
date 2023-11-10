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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
                                                 final LocalDate from,
                                                 final LocalDate to) {
        return commentRepository.findAllCommentsBetweenStartDateAndEndDateAndAllOpenCommentsBeforeStartDateForEmail(
                        from,
                        to,
                        employeeEmail
                )
                .stream()
                .map(commentMapper::mapDbCommentToDomainComment)
                .collect(Collectors.toList());
    }

    @Override
    public int finish(final Comment comment) {
        var commentEntity = commentRepository.findById(comment.getId());
        sendMail(Mail.COMMENT_CLOSED, commentEntity);
        return commentRepository.setStatusDone(comment.getId());
    }

    @Override
    public FinishedAndTotalComments countFinishedAndTotalComments(final String employeeMail,
                                                                  final LocalDate from,
                                                                  final LocalDate to) {
        Objects.requireNonNull(employeeMail, "'employeeMail' must not be null!");
        Objects.requireNonNull(from, "'from' date must not be null!");
        Objects.requireNonNull(to, "'to' date must not be null!");

        var allComments =
                commentRepository.findAllCommentsBetweenStartDateAndEndDateAndAllOpenCommentsBeforeStartDateForEmail(
                        from, to, employeeMail
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
                          final String currentMonthYear) {
        Objects.requireNonNull(employeeEmail, "'employeeEmail' must not be null");
        var stepEntry = StringUtils.isBlank(project)
                ? stepEntryService.findStepEntryForEmployeeAtStep(stepId, employeeEmail, assigneeEmail, currentMonthYear)
                : stepEntryService.findStepEntryForEmployeeAndProjectAtStep(stepId, employeeEmail, assigneeEmail, project, currentMonthYear);

        var comment = new com.gepardec.mega.db.entity.employee.Comment();
        comment.setMessage(message);
        comment.setStepEntry(stepEntry);
        comment.setSourceSystem(sourceSystem);

        commentRepository.save(comment);

        if (SourceSystem.MEGA.equals(comment.getSourceSystem())) {
            sendMail(Mail.COMMENT_CREATED, comment);
        }

        return commentMapper.mapDbCommentToDomainComment(comment);
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
            throw new EntityNotFoundException(String.format("No entity found for id = %d", id));
        }

        comment.setMessage(message);

        commentRepository.update(comment);
        sendMail(Mail.COMMENT_MODIFIED, comment);

        return commentMapper.mapDbCommentToDomainComment(comment);
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
        Map<String, String> mailParameter = new HashMap<>() {{
            put(MailParameter.CREATOR, creator);
            put(MailParameter.RECIPIENT, recipient);
            put(MailParameter.COMMENT, comment.getMessage());
        }};

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
