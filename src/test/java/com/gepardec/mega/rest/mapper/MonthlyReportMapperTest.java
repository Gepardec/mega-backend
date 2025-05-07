package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;
import com.gepardec.mega.rest.model.CommentDto;
import com.gepardec.mega.rest.model.EmployeeDto;
import com.gepardec.mega.rest.model.MonthlyReportDto;
import com.gepardec.mega.rest.model.PrematureEmployeeCheckDto;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class MonthlyReportMapperTest {
    @Inject
    MonthlyReportMapper mapper;

    @InjectMock
    EmployeeMapper employeeMapper;

    @InjectMock
    CommentMapper commentMapper;

    @InjectMock
    PrematureEmployeeCheckMapper prematureEmployeeCheckMapper;


    @Test
    void mapToDomain() {
        EmployeeDto employeeDto = EmployeeDto.builder().userId("007-jbond").build();
        Employee employee = Employee.builder().userId("007-jbond").build();

        when(employeeMapper.mapToDomain(employeeDto))
                .thenReturn(employee);

        CommentDto commentDto = CommentDto.builder().id(1L).build();
        Comment comment = Comment.builder().id(1L).build();

        when(commentMapper.mapListToDomain(any()))
                .thenReturn(Collections.singletonList(comment));

        PrematureEmployeeCheckDto prematureEmployeeCheckDto = PrematureEmployeeCheckDto.builder().id(1L).build();
        PrematureEmployeeCheck prematureEmployeeCheck = PrematureEmployeeCheck.builder().id(1L).build();

        when(prematureEmployeeCheckMapper.mapToDomain(prematureEmployeeCheckDto))
                .thenReturn(prematureEmployeeCheck);

        MonthlyReportDto dto = MonthlyReportDto.builder()
                .employee(employeeDto)
                .initialDate(LocalDate.of(2024, 3, 10))
                .timeWarnings(List.of())
                .journeyWarnings(List.of())
                .comments(List.of(commentDto))
                .employeeCheckState(EmployeeState.OPEN)
                .employeeCheckStateReason("TestReason")
                .otherChecksDone(true)
                .vacationDays(2)
                .homeofficeDays(1)
                .compensatoryDays(0)
                .nursingDays(0)
                .maternityLeaveDays(0)
                .externalTrainingDays(0)
                .conferenceDays(2)
                .maternityProtectionDays(0)
                .fatherMonthDays(0)
                .paidSpecialLeaveDays(1)
                .nonPaidVacationDays(0)
                .paidSickLeave(0)
                .vacationDayBalance(0.0)
                .billableTime("100.5")
                .totalWorkingTime("144.5")
                .paidSickLeave(0)
                .overtime(12.5)
                .guildLead(null)
                .internalProjectLead("Maria Musterfrau")
                .prematureEmployeeCheck(prematureEmployeeCheckDto)
                .personioId(123456)
                .build();

        MonthlyReport actual = mapper.mapToDomain(dto);

        assertThat(actual).isNotNull();
        assertThat(actual.getVacationDays()).isEqualTo(2);
        assertThat(actual.getConferenceDays()).isEqualTo(2);
        assertThat(actual.getHomeofficeDays()).isOne();
        assertThat(actual.getPaidSpecialLeaveDays()).isOne();
        assertThat(actual.getBillableTime()).isEqualTo("100.5");
        assertThat(actual.getOvertime()).isEqualTo(12.5);
        assertThat(actual.getTotalWorkingTime()).isEqualTo("144.5");
        assertThat(actual.getGuildLead()).isEqualTo("");
        assertThat(actual.getInternalProjectLead()).isEqualTo("Maria Musterfrau");
        assertThat(actual.getPersonioId()).isEqualTo(123456);
    }

    @Test
    void mapToDto() {
        EmployeeDto employeeDto = EmployeeDto.builder().userId("007-jbond").build();
        Employee employee = Employee.builder().userId("007-jbond").build();

        when(employeeMapper.mapToDto(employee))
                .thenReturn(employeeDto);

        CommentDto commentDto = CommentDto.builder().id(1L).build();
        Comment comment = Comment.builder().id(1L).build();

        when(commentMapper.mapListToDto(any()))
                .thenReturn(Collections.singletonList(commentDto));

        PrematureEmployeeCheckDto prematureEmployeeCheckDto = PrematureEmployeeCheckDto.builder().id(1L).build();
        PrematureEmployeeCheck prematureEmployeeCheck = PrematureEmployeeCheck.builder().id(1L).build();

        when(prematureEmployeeCheckMapper.mapToDto(prematureEmployeeCheck))
                .thenReturn(prematureEmployeeCheckDto);

        MonthlyReport report = MonthlyReport.builder()
                .employee(employee)
                .initialDate(LocalDate.of(2024, 3, 10))
                .timeWarnings(List.of())
                .journeyWarnings(List.of())
                .comments(List.of(comment))
                .employeeCheckState(EmployeeState.OPEN)
                .employeeCheckStateReason("TestReason")
                .otherChecksDone(true)
                .vacationDays(2)
                .homeofficeDays(1)
                .compensatoryDays(0)
                .nursingDays(0)
                .maternityLeaveDays(0)
                .externalTrainingDays(0)
                .maternityProtectionDays(0)
                .fatherMonthDays(0)
                .paidSpecialLeaveDays(1)
                .nonPaidVacationDays(0)
                .paidSickLeave(0)
                .vacationDayBalance(0.0)
                .billableTime("100.5")
                .totalWorkingTime("144.5")
                .paidSickLeave(0)
                .overtime(12.5)
                .guildLead("")
                .internalProjectLead(null)
                .prematureEmployeeCheck(prematureEmployeeCheck)
                .personioId(123456)
                .build();

        MonthlyReportDto actual = mapper.mapToDto(report);

        assertThat(actual).isNotNull();
        assertThat(actual.getVacationDays()).isEqualTo(2);
        assertThat(actual.getHomeofficeDays()).isOne();
        assertThat(actual.getPaidSpecialLeaveDays()).isOne();
        assertThat(actual.getBillableTime()).isEqualTo("100.5");
        assertThat(actual.getOvertime()).isEqualTo(12.5);
        assertThat(actual.getTotalWorkingTime()).isEqualTo("144.5");
        assertThat(actual.getGuildLead()).isEqualTo(null);
        assertThat(actual.getInternalProjectLead()).isEqualTo(null);
        assertThat(actual.getPersonioId()).isEqualTo(123456);
    }
}
