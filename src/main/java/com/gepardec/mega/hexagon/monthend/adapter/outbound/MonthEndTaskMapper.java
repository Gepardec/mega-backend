package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndCompletionPolicy;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface MonthEndTaskMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "taskIdToUuid")
    @Mapping(target = "monthValue", source = "month", qualifiedByName = "yearMonthToDate")
    @Mapping(target = "type", source = "type", qualifiedByName = "taskTypeToString")
    @Mapping(target = "projectId", source = "projectId", qualifiedByName = "projectIdToUuid")
    @Mapping(target = "subjectEmployeeId", source = "subjectEmployeeId", qualifiedByName = "userIdToUuid")
    @Mapping(target = "eligibleActorIds", source = "eligibleActorIds", qualifiedByName = "userIdsToUuids")
    @Mapping(target = "completionPolicy", source = "completionPolicy", qualifiedByName = "completionPolicyToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "taskStatusToString")
    @Mapping(target = "completedBy", source = "completedBy", qualifiedByName = "userIdToUuid")
    void updateEntity(MonthEndTask task, @MappingTarget MonthEndTaskEntity entity);

    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToTaskId")
    @Mapping(target = "month", source = "monthValue", qualifiedByName = "dateToYearMonth")
    @Mapping(target = "type", source = "type", qualifiedByName = "stringToTaskType")
    @Mapping(target = "projectId", source = "projectId", qualifiedByName = "uuidToProjectId")
    @Mapping(target = "subjectEmployeeId", source = "subjectEmployeeId", qualifiedByName = "uuidToUserId")
    @Mapping(target = "eligibleActorIds", source = "eligibleActorIds", qualifiedByName = "uuidsToUserIds")
    @Mapping(target = "completionPolicy", source = "completionPolicy", qualifiedByName = "stringToCompletionPolicy")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToTaskStatus")
    @Mapping(target = "completedBy", source = "completedBy", qualifiedByName = "uuidToUserId")
    MonthEndTask toDomain(MonthEndTaskEntity entity);

    @Named("taskIdToUuid")
    default UUID taskIdToUuid(MonthEndTaskId id) {
        return id == null ? null : id.value();
    }

    @Named("uuidToTaskId")
    default MonthEndTaskId uuidToTaskId(UUID id) {
        return id == null ? null : MonthEndTaskId.of(id);
    }

    @Named("projectIdToUuid")
    default UUID projectIdToUuid(ProjectId projectId) {
        return projectId == null ? null : projectId.value();
    }

    @Named("uuidToProjectId")
    default ProjectId uuidToProjectId(UUID projectId) {
        return projectId == null ? null : ProjectId.of(projectId);
    }

    @Named("userIdToUuid")
    default UUID userIdToUuid(UserId userId) {
        return userId == null ? null : userId.value();
    }

    @Named("uuidToUserId")
    default UserId uuidToUserId(UUID userId) {
        return userId == null ? null : UserId.of(userId);
    }

    @Named("yearMonthToDate")
    default LocalDate yearMonthToDate(YearMonth month) {
        return month == null ? null : month.atDay(1);
    }

    @Named("dateToYearMonth")
    default YearMonth dateToYearMonth(LocalDate monthValue) {
        return monthValue == null ? null : YearMonth.from(monthValue);
    }

    @Named("taskTypeToString")
    default String taskTypeToString(MonthEndTaskType type) {
        return type == null ? null : type.name();
    }

    @Named("stringToTaskType")
    default MonthEndTaskType stringToTaskType(String type) {
        return type == null ? null : MonthEndTaskType.valueOf(type);
    }

    @Named("completionPolicyToString")
    default String completionPolicyToString(MonthEndCompletionPolicy policy) {
        return policy == null ? null : policy.name();
    }

    @Named("stringToCompletionPolicy")
    default MonthEndCompletionPolicy stringToCompletionPolicy(String policy) {
        return policy == null ? null : MonthEndCompletionPolicy.valueOf(policy);
    }

    @Named("taskStatusToString")
    default String taskStatusToString(MonthEndTaskStatus status) {
        return status == null ? null : status.name();
    }

    @Named("stringToTaskStatus")
    default MonthEndTaskStatus stringToTaskStatus(String status) {
        return status == null ? null : MonthEndTaskStatus.valueOf(status);
    }

    @Named("userIdsToUuids")
    default Set<UUID> userIdsToUuids(Set<UserId> userIds) {
        if (userIds == null) {
            return Set.of();
        }
        return userIds.stream()
                .map(UserId::value)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Named("uuidsToUserIds")
    default Set<UserId> uuidsToUserIds(Set<UUID> userIds) {
        if (userIds == null) {
            return Set.of();
        }
        return userIds.stream()
                .map(UserId::of)
                .collect(Collectors.toUnmodifiableSet());
    }
}
