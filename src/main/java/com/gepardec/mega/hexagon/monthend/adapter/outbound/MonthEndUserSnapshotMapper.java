package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface MonthEndUserSnapshotMapper {

    @Mapping(target = "zepUsername", source = "zepProfile.username")
    @Mapping(target = "employmentPeriods", source = "zepProfile.employmentPeriods")
    MonthEndUserSnapshot toSnapshot(User user);
}
