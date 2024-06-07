package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.personio.commons.model.Attribute;
import com.gepardec.mega.personio.employees.PersonioEmployeeDto;
import com.gepardec.mega.rest.mapper.PersonioEmployeeMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class PersonioEmployeeMapperTest {

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
    public void mapToDto_whenFullDomain_thenReturnDtoWithRelevantFields() {
        assertThat(personioEmployeeMapper.mapToDto(domain)).usingRecursiveComparison().isEqualTo(dto);
    }
    @Test
    public void mapToDomain_whenDtoWithRelevantFields_thenReturnFullDomain() {
        assertThat(personioEmployeeMapper.mapToDomain(dto)).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    public void mapToDto_whenNullDomain_thenReturnNull() {
        assertThat(personioEmployeeMapper.mapToDto(null)).isNull();
    }

    @Test
    public void mapToDomain_whenNullDto_thenReturnNull() {
        assertThat(personioEmployeeMapper.mapToDomain(null)).isNull();
    }
}
