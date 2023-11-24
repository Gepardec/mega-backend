package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.rest.model.CommentDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CommentMapper implements DtoMapper<Comment, CommentDto> {

    @Override
    public CommentDto mapToDto(Comment object) {
        return CommentDto.builder()
                .id(object.getId())
                .message(object.getMessage())
                .authorEmail(object.getAuthorEmail())
                .authorName(object.getAuthorName())
                .updateDate(object.getUpdateDate())
                .state(object.getState())
                .build();
    }

    @Override
    public Comment mapToDomain(CommentDto object) {
        return Comment.builder()
                .id(object.getId())
                .message(object.getMessage())
                .authorEmail(object.getAuthorEmail())
                .authorName(object.getAuthorName())
                .updateDate(object.getUpdateDate())
                .state(object.getState())
                .build();
    }
}
