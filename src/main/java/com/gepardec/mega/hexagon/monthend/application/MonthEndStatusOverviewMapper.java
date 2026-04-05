package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewProject;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface MonthEndStatusOverviewMapper {

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "project", source = "project")
    MonthEndStatusOverviewItem toItem(MonthEndTask task, MonthEndProjectSnapshot project);

    MonthEndStatusOverviewProject toProject(MonthEndProjectSnapshot project);
}
