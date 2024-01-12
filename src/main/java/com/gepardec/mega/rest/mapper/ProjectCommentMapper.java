package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.db.entity.project.ProjectComment;
import com.gepardec.mega.rest.model.ProjectCommentDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProjectCommentMapper implements DtoMapper<ProjectComment, ProjectCommentDto> {
    @Override
    public ProjectComment mapToDomain(ProjectCommentDto object) {
        ProjectComment projectComment = new ProjectComment();
        projectComment.setId(object.getId());
        projectComment.setDate(object.getDate());
        projectComment.setComment(object.getComment());
        return projectComment;
    }

    @Override
    public ProjectCommentDto mapToDto(ProjectComment entity) {
        return ProjectCommentDto.builder()
                .id(entity.getId())
                .date(entity.getDate())
                .comment(entity.getComment())
                .projectName(entity.getProject().getName())
                .build();
    }
}
