package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistClarificationItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface MonthEndWorklistMapper {

    @Mapping(target = "taskId", source = "id")
    MonthEndWorklistItem toItem(MonthEndTask task);

    @Mapping(target = "clarificationId", source = "id")
    MonthEndWorklistClarificationItem toItem(MonthEndClarification clarification);
}
