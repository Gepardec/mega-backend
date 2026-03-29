package com.gepardec.mega.hexagon.project.adapter.outbound;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface ProjectMapper {

    @Mapping(target = "id", source = "id.value")
    void updateEntity(Project project, @MappingTarget ProjectEntity entity);

    default Project toDomain(ProjectEntity entity) {
        return Project.reconstitute(
                ProjectId.of(entity.getId()),
                entity.getZepId(),
                entity.getName(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getLeads() != null ? entity.getLeads() : Set.of()
        );
    }
}
