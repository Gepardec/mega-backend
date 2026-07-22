package com.gepardec.mega.hexagon.project.adapter.inbound.rest;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.generated.model.ProjectItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface ProjectRestMapper {

    ProjectRestMapper INSTANCE = Mappers.getMapper(ProjectRestMapper.class);

    @Mapping(target = "id", source = "id.value")
    ProjectItemDto toDto(Project project);

    default List<ProjectItemDto> toDtoList(List<Project> projects) {
        return projects == null ? List.of()
                : projects.stream().map(this::toDto).toList();
    }
}
