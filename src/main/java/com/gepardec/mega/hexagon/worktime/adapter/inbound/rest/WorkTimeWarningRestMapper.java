package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.WorkTimeWarningDto;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface WorkTimeWarningRestMapper {
    WorkTimeWarningDto toDto(WorkTimeWarning warning);

    List<WorkTimeWarningDto> toDto(List<WorkTimeWarning> warnings);

    default String map(WorkTimeWarningType type) {
        return type == null ? null : type.name();
    }
}
