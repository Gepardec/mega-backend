package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeProjectSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface WorkTimeProjectSnapshotMapper {

    WorkTimeProjectSnapshot toSnapshot(Project project);
}
