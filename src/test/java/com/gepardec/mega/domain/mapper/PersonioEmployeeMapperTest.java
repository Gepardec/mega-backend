package com.gepardec.mega.domain.mapper;

import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.personio.commons.model.Attribute;
import com.gepardec.mega.personio.employees.PersonioEmployeeDto;
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
                .id(1)
                .email("testuser@testmail.com")
                .firstName("Test")
                .lastName("User")
                .vacationDayBalance(25.35)
                .personalNumber("000")
                .guildLead("TestGuildLead")
                .internalProjectLead("TestLead")
                .build();

        dto = PersonioEmployeeDto.builder()
                .id(Attribute.ofValue(1))
                .email(Attribute.ofValue("testuser@testmail.com"))
                .firstName(Attribute.ofValue("Test"))
                .lastName(Attribute.ofValue("User"))
                .vacationDayBalance(Attribute.ofValue(25.35))
                .personalnummer(Attribute.ofValue("000"))
                .guildLead(Attribute.ofValue("TestGuildLead"))
                .internalProjectLead(Attribute.ofValue("TestLead"))
                .build();
    }

    @Test
    public void testMapToEntity() {
        assertThat(personioEmployeeMapper.mapToEntity(domain)).usingRecursiveComparison().isEqualTo(dto);
    }
    @Test
    public void testMapToDomain() {
        assertThat(personioEmployeeMapper.mapToDomain(dto)).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    public void testMapNull() {
        assertThat(personioEmployeeMapper.mapToEntity(null)).isNull();
        assertThat(personioEmployeeMapper.mapToDomain(null)).isNull();
    }
}
