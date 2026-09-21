package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.MonthEndOverviewClarificationEntryDto;
import com.gepardec.mega.hexagon.generated.model.MonthEndStatusOverviewDto;
import com.gepardec.mega.hexagon.generated.model.MonthEndStatusOverviewEntryDto;
import com.gepardec.mega.hexagon.generated.model.MonthEndTaskDto;
import com.gepardec.mega.hexagon.generated.model.MonthEndTaskGenerationDto;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.shared.adapter.inbound.rest.SharedRefRestMapper;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.InjectionStrategy;
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

@Mapper(
        componentModel = MappingConstants.ComponentModel.JAKARTA,
        uses = SharedRefRestMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface MonthEndRestMapper {

    MonthEndTaskGenerationDto toDto(MonthEndTaskGenerationResult result);

    @Mapping(target = "taskId", source = "id")
    MonthEndTaskDto toDto(MonthEndTask task);

    @Mapping(target = "taskId", source = "id")
    @Mapping(target = "project", source = "projectId")
    @Mapping(target = "subjectEmployee", source = "subjectEmployeeId")
    @Mapping(target = "canComplete", ignore = true)
    @Mapping(target = "completedBy", source = "completedBy")
    MonthEndStatusOverviewEntryDto toEntry(
            MonthEndTask task,
            @Context Map<ProjectId, ProjectRef> projectRefs,
            @Context Map<UserId, UserRef> userRefs,
            @Context UserId actorId
    );

    @AfterMapping
    default void enrichTaskEntry(
            MonthEndTask task,
            @MappingTarget MonthEndStatusOverviewEntryDto entry,
            @Context UserId actorId
    ) {
        entry.canComplete(task.canBeCompletedBy(actorId));
    }

    MonthEndStatusOverviewDto toDto(
            MonthEndStatusOverview overview,
            @Context Map<ProjectId, ProjectRef> projectRefs,
            @Context Map<UserId, UserRef> userRefs,
            @Context UserId actorId
    );

    @Mapping(target = "clarificationId", source = "id")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "subjectEmployee", source = "subjectEmployeeId")
    @Mapping(target = "resolvedBy", source = "resolvedBy")
    @Mapping(target = "canResolve", ignore = true)
    @Mapping(target = "canEditText", ignore = true)
    @Mapping(target = "canDelete", ignore = true)
    MonthEndOverviewClarificationEntryDto toClarificationEntry(
            MonthEndClarification clarification,
            @Context Map<UserId, UserRef> userRefs,
            @Context UserId actorId
    );

    @AfterMapping
    default void enrichClarificationEntry(
            MonthEndClarification clarification,
            @MappingTarget MonthEndOverviewClarificationEntryDto item,
            @Context UserId actorId
    ) {
        item.canResolve(clarification.canBeResolvedBy(actorId))
                .canEditText(clarification.canEditText(actorId))
                .canDelete(clarification.canDelete(actorId));
    }

    default ProjectRef resolveProjectRef(ProjectId projectId, @Context Map<ProjectId, ProjectRef> projectRefs) {
        ProjectRef project = projectRefs.get(projectId);
        if (project == null) {
            throw new IllegalStateException("project snapshot not found for project " + projectId.value());
        }
        return project;
    }

    default UserRef resolveUserRef(UserId userId, @Context Map<UserId, UserRef> userRefs) {
        return userId == null ? null : userRefs.get(userId);
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
