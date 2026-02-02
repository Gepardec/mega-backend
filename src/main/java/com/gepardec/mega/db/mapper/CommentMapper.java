package com.gepardec.mega.db.mapper;

import com.gepardec.mega.db.entity.employee.CommentEntity;
import com.gepardec.mega.domain.model.Comment;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CommentMapper implements EntityMapper<Comment, CommentEntity> {

    @Override
    public CommentEntity mapToEntity(Comment object) {
        CommentEntity comment = new CommentEntity();
        comment.setId(object.getId());
        comment.setMessage(object.getMessage());
        comment.setState(object.getState());
        comment.setSourceSystem(object.getSourceSystem());
        return comment;
    }

    @Override
    public Comment mapToDomain(CommentEntity dbComment) {
        return Comment
                .builder()
                .id(dbComment.getId())
                .authorEmail(dbComment.getStepEntry().getAssignee().getEmail())
                .authorName("%s %s".formatted(
                        dbComment.getStepEntry().getAssignee().getFirstname(),
                        dbComment.getStepEntry().getAssignee().getLastname())
                )
                .updateDate(dbComment.getUpdatedDate().toString())
                .message(dbComment.getMessage())
                .state(dbComment.getState())
                .sourceSystem(dbComment.getSourceSystem())
                .build();
    }
}
