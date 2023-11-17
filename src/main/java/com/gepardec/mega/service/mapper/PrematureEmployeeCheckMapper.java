package com.gepardec.mega.service.mapper;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PrematureEmployeeCheckMapper {

    public com.gepardec.mega.domain.model.PrematureEmployeeCheck mapToDomain(PrematureEmployeeCheck prematureEmployeeCheck) {
        return com.gepardec.mega.domain.model.PrematureEmployeeCheck.builder()
                .id(prematureEmployeeCheck.getId())
//                .stepEntryId(prematureEmployeeCheck.getStepEntry().getId())
//                .user(prematureEmployeeCheck.getStepEntry().getAssignee())
//                .forMonth(prematureEmployeeCheck.getStepEntry().getDate())
                .creationDate(prematureEmployeeCheck.getCreationDate())
                .build();
    }

    public List<com.gepardec.mega.domain.model.PrematureEmployeeCheck> mapListToDomain(List<PrematureEmployeeCheck> prematureEmployeeChecks) {
        return prematureEmployeeChecks.stream().map(this::mapToDomain).collect(Collectors.toList());
    }
}
