package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.WorkTimeBookingWarning;
import com.gepardec.mega.rest.model.WorkTimeBookingWarningDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WorkTimeBookingWarningMapper implements DtoMapper<WorkTimeBookingWarning, WorkTimeBookingWarningDto> {
    @Override
    public WorkTimeBookingWarningDto mapToDto(WorkTimeBookingWarning object) {
        return WorkTimeBookingWarningDto.builder()
                .name(object.getName())
                .warningDates(object.getWarningDates())
                .build();
    }

    @Override
    public WorkTimeBookingWarning mapToDomain(WorkTimeBookingWarningDto object) {
        return WorkTimeBookingWarning.builder()
                .name(object.getName())
                .warningDates(object.getWarningDates())
                .build();
    }
}
