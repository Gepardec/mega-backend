package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface MonthEndTaskMapper {

    @Mapping(target = "monthValue", source = "month")
    void updateEntity(MonthEndTask task, @MappingTarget MonthEndTaskEntity entity);

    @Mapping(target = "month", source = "monthValue")
    MonthEndTask toDomain(MonthEndTaskEntity entity);

    default UUID fromTaskId(MonthEndTaskId id) {
        return id == null ? null : id.value();
    }

    default MonthEndTaskId toTaskId(UUID id) {
        return id == null ? null : MonthEndTaskId.of(id);
    }

    default UUID fromProjectId(ProjectId projectId) {
        return projectId == null ? null : projectId.value();
    }

    default ProjectId toProjectId(UUID projectId) {
        return projectId == null ? null : ProjectId.of(projectId);
    }

    default UUID fromUserId(UserId userId) {
        return userId == null ? null : userId.value();
    }

    default UserId toUserId(UUID userId) {
        return userId == null ? null : UserId.of(userId);
    }

    default LocalDate toDate(YearMonth month) {
        return month == null ? null : month.atDay(1);
    }

    default YearMonth toYearMonth(LocalDate monthValue) {
        return monthValue == null ? null : YearMonth.from(monthValue);
    }
}
