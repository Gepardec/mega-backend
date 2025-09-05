package com.gepardec.mega.rest.impl;

import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.domain.model.SourceSystem;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.rest.api.CommentResource;
import com.gepardec.mega.rest.mapper.CommentMapper;
import com.gepardec.mega.rest.model.CommentDto;
import com.gepardec.mega.rest.model.NewCommentEntryDto;
import com.gepardec.mega.service.api.CommentService;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RequestScoped
@Authenticated
public class CommentResourceImpl implements CommentResource {

    @Inject
    CommentMapper mapper;

    @Inject
    CommentService commentService;

    @Override
    public Response finish(final CommentDto commentDto) {
        return Response.ok(commentService.finish(mapper.mapToDomain(commentDto))).build();
    }

    @Override
    public Response getAllCommentsForEmployee(String employeeEmail, String currentMonthYear) {
        List<Comment> commentsForEmployee = commentService.findCommentsForEmployee(
                employeeEmail,
                YearMonth.from(DateUtils.getFirstDayOfCurrentMonth(currentMonthYear))
        );
        return Response.ok(mapper.mapListToDto(commentsForEmployee)).build();
    }

    @Override
    public Response newCommentForEmployee(NewCommentEntryDto newComment) {
        Comment comment = commentService.create(
                newComment.getStepId(),
                SourceSystem.MEGA,
                newComment.getEmployeeEmail(),
                newComment.getComment(),
                newComment.getAssigneeEmail(),
                newComment.getProject(),
                YearMonth.from(LocalDate.parse(newComment.getCurrentMonthYear()))
        );

        return Response.ok(mapper.mapToDto(comment)).build();
    }

    @Override
    public Response deleteComment(Long id) {
        return Response.ok(commentService.delete(id)).build();
    }

    @Override
    public Response updateCommentForEmployee(CommentDto comment) {
        Comment updatedComment = commentService.update(comment.getId(), comment.getMessage());

        return Response.ok(mapper.mapToDto(updatedComment)).build();
    }
}
