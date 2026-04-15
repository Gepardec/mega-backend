package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.ZepEmployeeSyncData;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface ZepEmployeeMapper {

    @Mapping(target = "zepUsername", source = "employee.username")
    @Mapping(target = "email", source = "employee.email")
    @Mapping(target = "firstname", source = "employee.firstname")
    @Mapping(target = "lastname", source = "employee.lastname")
    @Mapping(target = "employmentPeriods", source = "employmentPeriods")
    ZepEmployeeSyncData toSyncData(ZepEmployee employee, EmploymentPeriods employmentPeriods);

    default ZepUsername toZepUsername(String username) {
        return username == null ? null : ZepUsername.of(username);
    }
}
