package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.user.domain.model.FullName;
import com.gepardec.mega.hexagon.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface MonthEndUserSnapshotMapper {

    @Mapping(target = "fullName", source = "name")
    @Mapping(target = "zepUsername", source = "zepUsername.value")
    MonthEndUserSnapshot toSnapshot(User user);

    default String map(FullName fullName) {
        if (fullName == null) {
            return null;
        }
        if (fullName.firstname() == null) {
            return fullName.lastname();
        }
        if (fullName.lastname() == null) {
            return fullName.firstname();
        }
        return "%s %s".formatted(fullName.firstname(), fullName.lastname());
    }
}
