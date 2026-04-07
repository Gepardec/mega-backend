package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndClarificationResponse;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndEmployeeReference;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndPreparationResponse;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndProjectReference;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndStatusOverviewEntry;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndStatusOverviewResponse;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndTaskGenerationResponse;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndTaskResponse;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndWorklistClarification;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndWorklistResponse;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndWorklistTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndEmployee;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndPreparationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProject;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklist;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistClarificationItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistItem;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface MonthEndRestMapper {

    MonthEndWorklistResponse toResponse(MonthEndWorklist worklist);

    MonthEndStatusOverviewResponse toResponse(MonthEndStatusOverview overview);

    @Mapping(target = "taskId", source = "id")
    MonthEndTaskResponse toResponse(MonthEndTask task);

    @Mapping(target = "clarificationId", source = "id")
    MonthEndClarificationResponse toResponse(MonthEndClarification clarification);

    MonthEndPreparationResponse toResponse(MonthEndPreparationResult result);

    MonthEndTaskGenerationResponse toResponse(MonthEndTaskGenerationResult result);

    MonthEndWorklistTask toResponse(MonthEndWorklistItem item);

    MonthEndWorklistClarification toResponse(MonthEndWorklistClarificationItem item);

    MonthEndStatusOverviewEntry toResponse(MonthEndStatusOverviewItem item);

    MonthEndProjectReference toResponse(MonthEndProject project);

    MonthEndEmployeeReference toResponse(MonthEndEmployee subjectEmployee);

    default String map(YearMonth month) {
        return month == null ? null : month.toString();
    }

    default UUID map(ProjectId projectId) {
        return projectId == null ? null : projectId.value();
    }

    default UUID map(UserId userId) {
        return userId == null ? null : userId.value();
    }

    default UUID map(MonthEndTaskId taskId) {
        return taskId == null ? null : taskId.value();
    }

    default UUID map(MonthEndClarificationId clarificationId) {
        return clarificationId == null ? null : clarificationId.value();
    }

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
