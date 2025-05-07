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
                .hasCreditCard(domain.getHasCreditCard() ? Attribute.ofValue("Ja") : Attribute.ofValue(""))
                .id(Attribute.ofValue(domain.getPersonioId()))
                .build();
    }

    @Override
    public PersonioEmployee mapToDomain(PersonioEmployeeDto dto) {
        if (dto == null) {
            return null;
        }

        //TODO: If vacation day balance is queried from Personio, it should be added here
        return PersonioEmployee.builder()
                .email(dto.email() == null ?
                        null : dto.email().getValue())
                .vacationDayBalance(0.0)
                .guildLead(dto.guildLead() == null ? null : dto.guildLead().getValue())
                .internalProjectLead(dto.internalProjectLead() == null ? null : dto.internalProjectLead().getValue())
                .hasCreditCard(dto.hasCreditCard().getValue().equals("Ja"))
                .personioId(dto.id().getValue())
                .build();
    }
}
