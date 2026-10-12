package com.gepardec.mega.hexagon.project.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.ProjectSettingsDto;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.shared.adapter.inbound.rest.SharedRefRestMapper;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.JAKARTA,
        uses = SharedRefRestMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ProjectRestMapper {

    @Mapping(target = "project", source = ".")
    ProjectSettingsDto toDto(Project project);

    ProjectRef toProjectRef(Project project);

    default List<ProjectSettingsDto> toDtoList(List<Project> projects) {
        return projects == null ? List.of()
                : projects.stream().map(this::toDto).toList();
    }
}
