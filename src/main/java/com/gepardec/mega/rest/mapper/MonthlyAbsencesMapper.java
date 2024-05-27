package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.MonthlyAbsences;
import com.gepardec.mega.rest.model.MonthlyAbsencesDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MonthlyAbsencesMapper implements DtoMapper<MonthlyAbsences, MonthlyAbsencesDto> {
    @Override
    public MonthlyAbsencesDto mapToDto(MonthlyAbsences object) {
        return MonthlyAbsencesDto.builder()
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
                .doctorsVisitingTime(object.getDoctorsVisitingTime())
                .availableVacationDays(object.getAvailableVacationDays())
                .build();
    }

    @Override
    public MonthlyAbsences mapToDomain(MonthlyAbsencesDto object) {
        return MonthlyAbsences.builder()
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
                .doctorsVisitingTime(object.getDoctorsVisitingTime())
                .availableVacationDays(object.getAvailableVacationDays())
                .build();
    }
}
