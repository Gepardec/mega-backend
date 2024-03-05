package com.gepardec.mega.domain.mapper;

import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.personio.commons.model.Attribute;
import com.gepardec.mega.personio.employees.PersonioEmployeeDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PersonioEmployeeMapper implements DomainMapper<PersonioEmployee, PersonioEmployeeDto> {
    @Override
    public PersonioEmployeeDto mapToEntity(PersonioEmployee object) {
        if (object == null) {
            return null;
        }
        return PersonioEmployeeDto.builder()
                .email(Attribute.ofValue(object.getEmail()))
                .vacationDayBalance(Attribute.ofValue(object.getVacationDayBalance()))
                .guildLead(Attribute.ofValue(object.getGuildLead()))
                .internalProjectLead(Attribute.ofValue(object.getInternalProjectLead()))
                .build();
    }

    @Override
    public PersonioEmployee mapToDomain(PersonioEmployeeDto object) {
        if (object == null) {
            return null;
        }

        return PersonioEmployee.builder()
                .email(object.email() == null ?
                        null : object.email().getValue())
                .vacationDayBalance(object.vacationDayBalance() == null ?
                        null : object.vacationDayBalance().getValue())
                .guildLead(object.guildLead() == null ?
                        null : object.guildLead().getValue())
                .internalProjectLead(object.internalProjectLead() == null ?
                        null : object.internalProjectLead().getValue())
                .build();
    }
}
