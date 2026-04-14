package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
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
public interface MonthEndClarificationMapper {

    @Mapping(target = "monthValue", source = "month")
    void updateEntity(MonthEndClarification clarification, @MappingTarget MonthEndClarificationEntity entity);

    @Mapping(target = "month", source = "monthValue")
    MonthEndClarification toDomain(MonthEndClarificationEntity entity);

    default UUID fromClarificationId(MonthEndClarificationId id) {
        return id == null ? null : id.value();
    }

    default MonthEndClarificationId toClarificationId(UUID id) {
        return id == null ? null : MonthEndClarificationId.of(id);
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
