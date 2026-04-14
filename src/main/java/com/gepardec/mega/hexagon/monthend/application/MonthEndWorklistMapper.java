package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistClarificationItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistItem;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface MonthEndWorklistMapper {

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "type", source = "task.type")
    @Mapping(target = "project", source = "project")
    @Mapping(target = "subjectEmployee", source = "subjectEmployee")
    MonthEndWorklistItem toItem(MonthEndTask task, ProjectRef project, UserRef subjectEmployee);

    @Mapping(target = "clarificationId", source = "id")
    MonthEndWorklistClarificationItem toItem(MonthEndClarification clarification);
}
