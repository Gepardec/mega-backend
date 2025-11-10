package com.gepardec.mega.domain.mapper;

import com.gepardec.mega.domain.model.Comment;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CommentMapper implements EntityMapper<Comment, com.gepardec.mega.db.entity.employee.Comment> {

    @Override
    public com.gepardec.mega.db.entity.employee.Comment mapToEntity(Comment object) {
        com.gepardec.mega.db.entity.employee.Comment comment = new com.gepardec.mega.db.entity.employee.Comment();
        comment.setId(object.getId());
        comment.setMessage(object.getMessage());
        comment.setState(object.getState());
        comment.setSourceSystem(object.getSourceSystem());
        return comment;
    }

    @Override
    public Comment mapToDomain(com.gepardec.mega.db.entity.employee.Comment dbComment) {
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
