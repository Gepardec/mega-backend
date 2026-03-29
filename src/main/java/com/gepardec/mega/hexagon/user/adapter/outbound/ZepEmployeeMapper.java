package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.RegularWorkingTimes;
import com.gepardec.mega.hexagon.user.domain.model.ZepProfile;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface ZepEmployeeMapper {

    @Mapping(target = "username", source = "employee.username")
    @Mapping(target = "email", source = "employee.email")
    @Mapping(target = "firstname", source = "employee.firstname")
    @Mapping(target = "lastname", source = "employee.lastname")
    @Mapping(target = "title", source = "employee.title")
    @Mapping(target = "releaseDate", source = "employee.releaseDate")
    @Mapping(target = "salutation", source = "employee.salutation.name")
    @Mapping(target = "language", source = "employee.language.id")
    @Mapping(target = "workDescription", ignore = true)
    @Mapping(target = "employmentPeriods", source = "employmentPeriods")
    @Mapping(target = "regularWorkingTimes", source = "regularWorkingTimes")
    ZepProfile toZepProfile(ZepEmployee employee, EmploymentPeriods employmentPeriods, RegularWorkingTimes regularWorkingTimes);
}
