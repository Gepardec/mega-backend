package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.personio.commons.model.Attribute;
import com.gepardec.mega.personio.employees.PersonioEmployeeDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PersonioEmployeeMapper implements DtoMapper<PersonioEmployee, PersonioEmployeeDto> {
    @Override
    public PersonioEmployeeDto mapToDto(PersonioEmployee domain) {
        if (domain == null) {
            return null;
        }

        return PersonioEmployeeDto.builder()
                .email(Attribute.ofValue(domain.getEmail()))
                .guildLead(Attribute.ofValue(domain.getGuildLead()))
                .internalProjectLead(Attribute.ofValue(domain.getInternalProjectLead()))
                .build();
    }

    @Override
    public PersonioEmployee mapToDomain(PersonioEmployeeDto dto) {
        if (dto == null) {
            return null;
        }

        return PersonioEmployee.builder()
                .email(dto.email() == null ?
                        null : dto.email().getValue())
                .guildLead(dto.guildLead() == null ?
                        null : dto.guildLead().getValue())
                .internalProjectLead(dto.internalProjectLead() == null ?
                        null : dto.internalProjectLead().getValue())
                .build();
    }
}
