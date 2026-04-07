package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndEmployee;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProject;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistClarificationItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface MonthEndWorklistMapper {

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "type", source = "task.type")
    @Mapping(target = "project", source = "project")
    @Mapping(target = "subjectEmployee", source = "subjectEmployee")
    MonthEndWorklistItem toItem(MonthEndTask task, MonthEndProjectSnapshot project, MonthEndUserSnapshot subjectEmployee);

    MonthEndProject toProject(MonthEndProjectSnapshot project);

    MonthEndEmployee toSubjectEmployee(MonthEndUserSnapshot subjectEmployee);

    @Mapping(target = "clarificationId", source = "id")
    MonthEndWorklistClarificationItem toItem(MonthEndClarification clarification);
}
