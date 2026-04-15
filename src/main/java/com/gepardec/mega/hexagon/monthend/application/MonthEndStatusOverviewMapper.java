package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface MonthEndStatusOverviewMapper {

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "type", source = "task.type")
    @Mapping(target = "status", source = "task.status")
    @Mapping(target = "project", source = "project")
    @Mapping(target = "subjectEmployee", source = "subjectEmployee")
    @Mapping(target = "canComplete", source = "canComplete")
    @Mapping(target = "completedBy", source = "task.completedBy")
    MonthEndStatusOverviewItem toItem(
            MonthEndTask task,
            ProjectRef project,
            UserRef subjectEmployee,
            boolean canComplete
    );
}
