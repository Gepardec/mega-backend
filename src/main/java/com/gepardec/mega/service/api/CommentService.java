package com.gepardec.mega.service.api;

import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.domain.model.FinishedAndTotalComments;
import com.gepardec.mega.domain.model.SourceSystem;

import java.time.YearMonth;
import java.util.List;

public interface CommentService {
    List<Comment> findCommentsForEmployee(final String employeeEmail, YearMonth payrollMonth);

    int finish(final Comment comment);

    FinishedAndTotalComments countFinishedAndTotalComments(final String employeeMail, YearMonth payrollMonth);

    Comment create(Long stepId, SourceSystem sourceSystem, String employeeEmail, String comment, String assigneeEmail,
                   String project, YearMonth payrollMonth);

    boolean delete(Long id);

    Comment update(Long id, String message);
}
