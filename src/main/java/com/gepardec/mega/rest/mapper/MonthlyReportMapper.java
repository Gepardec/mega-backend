package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;
import com.gepardec.mega.rest.model.MonthlyReportDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MonthlyReportMapper implements DtoMapper<MonthlyReport, MonthlyReportDto> {

    @Inject
    EmployeeMapper employeeMapper;

    @Override
    public MonthlyReportDto mapToDto(MonthlyReport object) {
        return MonthlyReportDto.builder()
                .employee(employeeMapper.mapToDto(object.getEmployee()))
                .initialDate(object.getInitialDate())
                .timeWarnings(object.getTimeWarnings())
                .journeyWarnings(object.getJourneyWarnings())
                .comments(object.getComments()) //TODO: CommentDTO
                .employeeCheckState(object.getEmployeeCheckState())
                .employeeCheckStateReason(object.getEmployeeCheckStateReason())
                .internalCheckState(object.getInternalCheckState())
                .isAssigned(object.isAssigned())
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
                .build();
    }

    @Override
    public MonthlyReport mapToDomain(MonthlyReportDto object) {
        return MonthlyReport.builder()
                .employee(employeeMapper.mapToDomain(object.getEmployee()))
                .initialDate(object.getInitialDate())
                .timeWarnings(object.getTimeWarnings())
                .journeyWarnings(object.getJourneyWarnings())
                .comments(object.getComments())
                .employeeCheckState(object.getEmployeeCheckState())
                .employeeCheckStateReason(object.getEmployeeCheckStateReason())
                .internalCheckState(object.getInternalCheckState())
                .isAssigned(object.isAssigned())
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
                .build();
    }

}
