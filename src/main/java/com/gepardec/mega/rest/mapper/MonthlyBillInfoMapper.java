package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.MonthlyBillInfo;
import com.gepardec.mega.rest.model.MonthlyBillInfoDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MonthlyBillInfoMapper implements DtoMapper<MonthlyBillInfo, MonthlyBillInfoDto> {
    @Override
    public MonthlyBillInfoDto mapToDto(MonthlyBillInfo object) {
        return MonthlyBillInfoDto.builder()
                .sumBills(object.getSumBills())
                .sumPrivateBills(object.getSumPrivateBills())
                .sumCompanyBills(object.getSumCompanyBills())
                .hasAttachmentWarnings(object.getHasAttachmentWarnings())
                .employeeHasCreditCard(object.getEmployeeHasCreditCard())
                .build();
    }

    @Override
    public MonthlyBillInfo mapToDomain(MonthlyBillInfoDto object) {
        return MonthlyBillInfo.builder()
                .sumBills(object.getSumBills())
                .sumPrivateBills(object.getSumPrivateBills())
                .sumCompanyBills(object.getSumCompanyBills())
                .hasAttachmentWarnings(object.getHasAttachmentWarnings())
                .employeeHasCreditCard(object.getEmployeeHasCreditCard())
                .build();
    }
}
