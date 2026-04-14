package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.user.domain.model.FullName;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeUserSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface WorkTimeUserSnapshotMapper {

    @Mapping(target = "fullName", source = "name", qualifiedByName = "toFullName")
    @Mapping(target = "zepUsername", source = "zepUsername", qualifiedByName = "toZepUsername")
    WorkTimeUserSnapshot toSnapshot(User user);

    @Named("toFullName")
    default String toFullName(FullName fullName) {
        return (fullName.firstname() + " " + fullName.lastname()).trim();
    }

    @Named("toZepUsername")
    default String toZepUsername(ZepUsername zepUsername) {
        return zepUsername == null ? null : zepUsername.value();
    }
}
