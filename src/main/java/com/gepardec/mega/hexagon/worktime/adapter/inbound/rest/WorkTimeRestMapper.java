package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.WorkTimeReportDto;
import com.gepardec.mega.hexagon.shared.adapter.inbound.rest.SharedRefRestMapper;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.YearMonth;
import java.util.UUID;

@Mapper(
        componentModel = MappingConstants.ComponentModel.JAKARTA,
        uses = SharedRefRestMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface WorkTimeRestMapper {

    WorkTimeReportDto toDto(WorkTimeReport report);

    default String map(YearMonth month) {
        return month == null ? null : month.toString();
    }

    default UUID map(UserId userId) {
        return userId == null ? null : userId.value();
    }

    default UUID map(ProjectId projectId) {
        return projectId == null ? null : projectId.value();
    }

    default String map(FullName fullName) {
        return fullName == null ? null : fullName.displayName();
    }
}
