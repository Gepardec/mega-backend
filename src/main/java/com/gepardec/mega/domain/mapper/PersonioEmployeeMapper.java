package com.gepardec.mega.domain.mapper;

import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.personio.employees.PersonioEmployeeDto;
import com.gepardec.mega.personio.commons.model.Attribute;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PersonioEmployeeMapper implements DomainMapper<PersonioEmployee, PersonioEmployeeDto>{
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
                .email(object.getEmail()== null ?
                        null : object.getEmail().getValue())
                .vacationDayBalance(object.getVacationDayBalance() == null ?
                        null : object.getVacationDayBalance().getValue())
                .guildLead(object.getGuildLead() == null ?
                        null : object.getGuildLead().getValue())
                .internalProjectLead(object.getInternalProjectLead() == null ?
                        null : object.getInternalProjectLead().getValue())
                .build();
    }
}
