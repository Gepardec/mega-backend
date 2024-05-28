package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.MonthlyOfficeTimes;
import com.gepardec.mega.rest.model.MonthlyOfficeTimesDto;

public class MonthlyOfficeTimesMapper implements DtoMapper<MonthlyOfficeTimesDto, MonthlyOfficeTimes> {
    @Override
    public MonthlyOfficeTimes mapToDto(MonthlyOfficeTimesDto object) {
        return MonthlyOfficeTimes.builder()
                .homeOfficeDays(object.getHomeofficeDays())
                .fridaysAtTheOffice(object.getFridaysAtTheOffice())
                .officeDays(object.getOfficeDays())
                .build();
    }

    @Override
    public MonthlyOfficeTimesDto mapToDomain(MonthlyOfficeTimes object) {
        return MonthlyOfficeTimesDto.builder()
                .homeOfficeDays(object.getHomeofficeDays())
                .fridaysAtTheOffice(object.getFridaysAtTheOffice())
                .officeDays(object.getOfficeDays())
                .build();
    }
}
