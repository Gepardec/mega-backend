package com.gepardec.mega.hexagon.project.adapter.outbound;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface ProjectMapper {

    @Mapping(target = "id", source = "id.value")
    void updateEntity(Project project, @MappingTarget ProjectEntity entity);

    Project toDomain(ProjectEntity entity);

    default ProjectId toProjectId(UUID id) {
        return ProjectId.of(id);
    }

    default Set<UUID> fromLeadIds(Set<UserId> leadIds) {
        if (leadIds == null) {
            return Set.of();
        }
        return leadIds.stream()
                .map(UserId::value)
                .collect(Collectors.toSet());
    }

    default Set<UserId> toLeadIds(Set<UUID> leadIds) {
        if (leadIds == null) {
            return Set.of();
        }
        return leadIds.stream()
                .map(UserId::of)
                .collect(Collectors.toUnmodifiableSet());
    }
}
