package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.db.entity.project.ProjectCommentEntity;
import com.gepardec.mega.rest.model.ProjectCommentDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProjectCommentMapper implements DtoMapper<ProjectCommentEntity, ProjectCommentDto> {

    @Override
    public ProjectCommentEntity mapToDomain(ProjectCommentDto object) {
        ProjectCommentEntity projectComment = new ProjectCommentEntity();
        projectComment.setId(object.getId());
        projectComment.setDate(object.getDate());
        projectComment.setComment(object.getComment());
        return projectComment;
    }

    @Override
    public ProjectCommentDto mapToDto(ProjectCommentEntity entity) {
        return ProjectCommentDto.builder()
                .id(entity.getId())
                .date(entity.getDate())
                .comment(entity.getComment())
                .projectName(entity.getProject().getName())
                .build();
    }
}
