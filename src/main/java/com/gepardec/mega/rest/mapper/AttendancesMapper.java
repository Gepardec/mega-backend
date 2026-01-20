package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.Attendances;
import com.gepardec.mega.rest.model.AttendancesDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AttendancesMapper implements DtoMapper<Attendances, AttendancesDto> {

    @Override
    public AttendancesDto mapToDto(Attendances object) {
        return new AttendancesDto(
                object.totalWorkingTimeHours(),
                object.overtimeHours(),
                object.billableTimeHours(),
                object.billablePercentage()
        );
    }

    @Override
    public Attendances mapToDomain(AttendancesDto object) {
        return new Attendances(
                object.totalWorkingTimeHours(),
                object.overtimeHours(),
                object.billableTimeHours(),
                object.billablePercentage()
        );
    }
}
