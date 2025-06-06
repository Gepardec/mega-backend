package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.personio.commons.model.Attribute;
import com.gepardec.mega.personio.employees.PersonioEmployeeDto;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class PersonioEmployeeMapperTest {

    @Inject
    PersonioEmployeeMapper personioEmployeeMapper;

    PersonioEmployee domain;

    PersonioEmployeeDto dto;

    @BeforeEach
    void setUp() {
        domain = PersonioEmployee.builder()
                .email("testuser@testmail.com")
                .vacationDayBalance(0.0)
                .guildLead("TestGuildLead")
                .hasCreditCard(true)
                .internalProjectLead("TestLead")
                .build();

        dto = PersonioEmployeeDto.builder()
                .email(Attribute.ofValue("testuser@testmail.com"))
                .guildLead(Attribute.ofValue("TestGuildLead"))
                .hasCreditCard(Attribute.ofValue("Ja"))
                .internalProjectLead(Attribute.ofValue("TestLead"))
                .build();
    }

    @Test
    void mapToDto_whenFullDomain_thenReturnDtoWithRelevantFields() {
        assertThat(personioEmployeeMapper.mapToDto(domain)).usingRecursiveComparison().isEqualTo(dto);
    }

    @Test
    void mapToDto_whenHasCreditCardFalse_thenReturnDtoWithRelevantFields() {
        PersonioEmployee domain = PersonioEmployee.builder()
                .email("testuser@testmail.com")
                .vacationDayBalance(0.0)
                .guildLead(null)
                .hasCreditCard(false)
                .internalProjectLead(null)
                .build();

        PersonioEmployeeDto actual = personioEmployeeMapper.mapToDto(domain);

        assertThat(actual.hasCreditCard().getValue()).isEqualTo("");
    }


    @Test
    void mapToDomain_whenDtoWithGuildLeadAndInternalProjectLeadNull_thenReturnFullDomain() {
        PersonioEmployeeDto personioEmployeeDto = PersonioEmployeeDto.builder()
                .email(Attribute.ofValue("testuser@testmail.com"))
                .hasCreditCard(Attribute.ofValue("Ja"))
                .build();


        PersonioEmployee actualDomain = personioEmployeeMapper.mapToDomain(personioEmployeeDto);

        assertThat(actualDomain.getGuildLead()).isNull();
        assertThat(actualDomain.getInternalProjectLead()).isNull();

    }

    @Test
    void mapToDomain_whenDtoWithRelevantFields_thenReturnFullDomain() {
        assertThat(personioEmployeeMapper.mapToDomain(dto)).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void mapToDto_whenNullDomain_thenReturnNull() {
        assertThat(personioEmployeeMapper.mapToDto(null)).isNull();
    }

    @Test
    void mapToDomain_whenNullDto_thenReturnNull() {
        assertThat(personioEmployeeMapper.mapToDomain(null)).isNull();
    }
}
