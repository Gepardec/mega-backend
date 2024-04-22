package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.Bill;
import com.gepardec.mega.rest.model.BillDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BillMapper implements DtoMapper<Bill, BillDto> {
    @Override
    public BillDto mapToDto(Bill object) {
        return BillDto.builder()
                .billDate(object.getBillDate())
                .billType(object.getBillType())
                .bruttoValue(object.getBruttoValue())
                .paymentMethodType(object.getPaymentMethodType())
                .projectName(object.getProjectName())
                .attachmentBase64(object.getAttachmentBase64String())
                .build();
    }

    @Override
    public Bill mapToDomain(BillDto object) {
        return Bill.builder()
                .billDate(object.getBillDate())
                .billType(object.getBillType())
                .bruttoValue(object.getBruttoValue())
                .paymentMethodType(object.getPaymentMethodType())
                .projectName(object.getProjectName())
                .attachmentBase64(object.getAttachmentBase64String())
                .build();
    }
}
