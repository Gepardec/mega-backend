package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.WorkTimeEmployeeReference;
import com.gepardec.mega.hexagon.generated.model.WorkTimeProjectReference;
import com.gepardec.mega.hexagon.generated.model.WorkTimeReportResponse;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEmployee;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeProject;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.YearMonth;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface WorkTimeRestMapper {

    WorkTimeReportResponse toResponse(WorkTimeReport report);

    WorkTimeEmployeeReference toResponse(WorkTimeEmployee employee);

    WorkTimeProjectReference toResponse(WorkTimeProject project);

    default String map(YearMonth month) {
        return month == null ? null : month.toString();
    }

    default UUID map(UserId userId) {
        return userId == null ? null : userId.value();
    }

    default UUID map(ProjectId projectId) {
        return projectId == null ? null : projectId.value();
    }
}
