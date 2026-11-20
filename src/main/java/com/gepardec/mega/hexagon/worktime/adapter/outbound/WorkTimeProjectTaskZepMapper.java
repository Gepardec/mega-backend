package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.worktime.domain.model.ProjectTask;
import com.gepardec.mega.zep.rest.dto.ZepProjectTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface WorkTimeProjectTaskZepMapper {

    @Mapping(target = "projectZepId", source = "projectId")
    @Mapping(target = "zepTaskId", source = "id")
    ProjectTask toDomain(ZepProjectTask task);
}
