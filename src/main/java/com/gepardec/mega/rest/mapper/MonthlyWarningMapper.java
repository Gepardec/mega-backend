package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.MonthlyWarning;
import com.gepardec.mega.rest.model.MonthlyWarningDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MonthlyWarningMapper implements DtoMapper<MonthlyWarning, MonthlyWarningDto> {
    @Override
    public MonthlyWarningDto mapToDto(MonthlyWarning object) {
        return MonthlyWarningDto.builder()
                .name(object.getName())
                .dateValuesWhenWarningsOccurred(object.getDateValuesWhenWarningsOccurred())
                .build();
    }

    @Override
    public MonthlyWarning mapToDomain(MonthlyWarningDto object) {
        return MonthlyWarning.builder()
                .name(object.getName())
                .dateValuesWhenWarningsOccurred(object.getDateValuesWhenWarningsOccurred())
                .build();
    }
}
