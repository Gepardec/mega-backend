package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.MonthlyOfficeDays;
import com.gepardec.mega.rest.model.MonthlyOfficeDaysDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MonthlyOfficeDaysMapper implements DtoMapper<MonthlyOfficeDays, MonthlyOfficeDaysDto> {
    @Override
    public MonthlyOfficeDaysDto mapToDto(MonthlyOfficeDays object) {
        return MonthlyOfficeDaysDto.builder()
                .homeOfficeDays(object.getHomeofficeDays())
                .fridaysAtTheOffice(object.getFridaysAtTheOffice())
                .officeDays(object.getOfficeDays())
                .build();
    }

    @Override
    public MonthlyOfficeDays mapToDomain(MonthlyOfficeDaysDto object) {
        return MonthlyOfficeDays.builder()
                .homeOfficeDays(object.getHomeofficeDays())
                .fridaysAtTheOffice(object.getFridaysAtTheOffice())
                .officeDays(object.getOfficeDays())
                .build();
    }
}
