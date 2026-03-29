package com.gepardec.mega.hexagon.project.adapter.outbound;

import com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface ZepProjectMapper {

    @Mapping(target = "zepId", source = "id")
    @Mapping(target = "startDate", source = "startDate", qualifiedByName = "toLocalDate")
    @Mapping(target = "endDate", source = "endDate", qualifiedByName = "toLocalDate")
    ZepProjectProfile toProfile(ZepProject project);

    @Named("toLocalDate")
    default LocalDate toLocalDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate() : null;
    }
}
