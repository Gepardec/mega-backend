package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.MonthEndEmployeeReference;
import com.gepardec.mega.hexagon.generated.model.MonthEndOverviewClarificationEntry;
import com.gepardec.mega.hexagon.generated.model.MonthEndPreparationResponse;
import com.gepardec.mega.hexagon.generated.model.MonthEndProjectReference;
import com.gepardec.mega.hexagon.generated.model.MonthEndStatusOverviewEntry;
import com.gepardec.mega.hexagon.generated.model.MonthEndStatusOverviewResponse;
import com.gepardec.mega.hexagon.generated.model.MonthEndTaskGenerationResponse;
import com.gepardec.mega.hexagon.generated.model.MonthEndTaskResponse;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndPreparationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface MonthEndRestMapper {

    MonthEndTaskGenerationResponse toResponse(MonthEndTaskGenerationResult result);

    @Mapping(target = "taskId", source = "id")
    MonthEndTaskResponse toResponse(MonthEndTask task);

    MonthEndStatusOverviewEntry toResponse(MonthEndStatusOverviewItem item);

    MonthEndProjectReference toResponse(ProjectRef project);

    MonthEndEmployeeReference toResponse(UserRef subjectEmployee);

    MonthEndStatusOverviewResponse toResponse(
            MonthEndStatusOverview overview,
            @Context Map<UserId, UserRef> userRefs,
            @Context UserId actorId
    );

    MonthEndPreparationResponse toResponse(
            MonthEndPreparationResult result,
            @Context Map<UserId, UserRef> userRefs,
            @Context UserId actorId
    );

    @Mapping(target = "clarificationId", source = "id")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "subjectEmployee", ignore = true)
    @Mapping(target = "resolvedBy", ignore = true)
    @Mapping(target = "canResolve", ignore = true)
    @Mapping(target = "canEditText", ignore = true)
    @Mapping(target = "canDelete", ignore = true)
    MonthEndOverviewClarificationEntry toClarificationEntry(
            MonthEndClarification clarification,
            @Context Map<UserId, UserRef> userRefs,
            @Context UserId actorId
    );

    @AfterMapping
    default void enrichClarificationEntry(
            MonthEndClarification clarification,
            @MappingTarget MonthEndOverviewClarificationEntry item,
            @Context Map<UserId, UserRef> userRefs,
            @Context UserId actorId
    ) {
        UserRef subjectRef = clarification.subjectEmployeeId() != null
                ? userRefs.get(clarification.subjectEmployeeId())
                : null;
        UserRef resolvedByRef = clarification.resolvedBy() != null
                ? userRefs.get(clarification.resolvedBy())
                : null;
        item.createdBy(toResponse(userRefs.get(clarification.createdBy())))
                .subjectEmployee(subjectRef != null ? toResponse(subjectRef) : null)
                .resolvedBy(resolvedByRef != null ? toResponse(resolvedByRef) : null)
                .canResolve(clarification.canBeResolvedBy(actorId))
                .canEditText(clarification.canEditText(actorId))
                .canDelete(clarification.canDelete(actorId));
    }

    default String map(YearMonth month) {
        return month == null ? null : month.toString();
    }

    default UUID map(ProjectId projectId) {
        return projectId == null ? null : projectId.value();
    }

    default UUID map(UserId userId) {
        return userId == null ? null : userId.value();
    }

    default String map(FullName fullName) {
        return fullName == null ? null : fullName.displayName();
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
