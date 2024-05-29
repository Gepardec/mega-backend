package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;
import com.gepardec.mega.rest.model.MonthlyReportDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class MonthlyReportMapper implements DtoMapper<MonthlyReport, MonthlyReportDto> {

    @Inject
    EmployeeMapper employeeMapper;

    @Inject
    CommentMapper commentMapper;

    @Inject
    PrematureEmployeeCheckMapper prematureEmployeeCheckMapper;

    @Override
    public MonthlyReportDto mapToDto(MonthlyReport object) {
        return MonthlyReportDto.builder()
                .employee(employeeMapper.mapToDto(object.getEmployee()))
                .initialDate(object.getInitialDate())
                .timeWarnings(object.getTimeWarnings())
                .journeyWarnings(object.getJourneyWarnings())
                .comments(commentMapper.mapListToDto(object.getComments()))
                .employeeCheckState(object.getEmployeeCheckState())
                .employeeCheckStateReason(object.getEmployeeCheckStateReason())
                .internalCheckState(object.getInternalCheckState())
                .employeeProgresses(object.getEmployeeProgresses())
                .otherChecksDone(object.isOtherChecksDone())
                .vacationDays(object.getVacationDays())
                .homeofficeDays(object.getHomeofficeDays())
                .compensatoryDays(object.getCompensatoryDays())
                .nursingDays(object.getNursingDays())
                .maternityLeaveDays(object.getMaternityLeaveDays())
                .fatherMonthDays(object.getFatherMonthDays())
                .paidSpecialLeaveDays(object.getPaidSpecialLeaveDays())
                .nonPaidVacationDays(object.getNonPaidVacationDays())
                .vacationDayBalance(object.getVacationDayBalance())
                .billableTime(object.getBillableTime())
                .totalWorkingTime(object.getTotalWorkingTime())
                .paidSickLeave(object.getPaidSickLeave())
                .overtime(object.getOvertime())
                .guildLead(emptyToDtoFormat(object.getGuildLead()))
                .internalProjectLead(emptyToDtoFormat(object.getInternalProjectLead()))
                .hasCreditCard(object.getHasCreditCard())
                .prematureEmployeeCheck(
                        Optional.ofNullable(object.getPrematureEmployeeCheck())
                                .map(prematureEmployeeCheckMapper::mapToDto)
                                .orElse(null)
                )
                .build();
    }

    @Override
    public MonthlyReport mapToDomain(MonthlyReportDto object) {
        return MonthlyReport.builder()
                .employee(employeeMapper.mapToDomain(object.getEmployee()))
                .initialDate(object.getInitialDate())
                .timeWarnings(object.getTimeWarnings())
                .journeyWarnings(object.getJourneyWarnings())
                .comments(commentMapper.mapListToDomain(object.getComments()))
                .employeeCheckState(object.getEmployeeCheckState())
                .employeeCheckStateReason(object.getEmployeeCheckStateReason())
                .internalCheckState(object.getInternalCheckState())
                .employeeProgresses(object.getEmployeeProgresses())
                .otherChecksDone(object.isOtherChecksDone())
                .vacationDays(object.getVacationDays())
                .homeofficeDays(object.getHomeofficeDays())
                .compensatoryDays(object.getCompensatoryDays())
                .nursingDays(object.getNursingDays())
                .maternityLeaveDays(object.getMaternityLeaveDays())
                .externalTrainingDays(object.getExternalTrainingDays())
                .conferenceDays(object.getConferenceDays())
                .maternityProtectionDays(object.getMaternityProtectionDays())
                .fatherMonthDays(object.getFatherMonthDays())
                .paidSpecialLeaveDays(object.getPaidSpecialLeaveDays())
                .nonPaidVacationDays(object.getNonPaidVacationDays())
                .paidSickLeave(object.getPaidSickLeave())
                .vacationDayBalance(object.getVacationDayBalance())
                .billableTime(object.getBillableTime())
                .totalWorkingTime(object.getTotalWorkingTime())
                .paidSickLeave(object.getPaidSickLeave())
                .overtime(object.getOvertime())
                .guildLead(emptyToDomainFormat(object.getGuildLead()))
                .internalProjectLead(emptyToDomainFormat(object.getInternalProjectLead()))
                .hasCreditCard(object.getHasCreditCard())
                .prematureEmployeeCheck(prematureEmployeeCheckMapper.mapToDomain(object.getPrematureEmployeeCheck()))
                .build();
    }

    private static String emptyToDtoFormat(String value) {
        if (value == null) {
            return value;
        }
        return value.isEmpty() ? null : value;
    }

    private static String emptyToDomainFormat(String value) {
        return value == null ? "" : value;
    }
}
