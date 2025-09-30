package com.gepardec.mega.service.mapper;

import com.gepardec.mega.db.entity.enterprise.EnterpriseEntry;
import com.gepardec.mega.domain.model.ProjectState;
import com.gepardec.mega.rest.model.EnterpriseEntryDto;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class EnterpriseEntryMapper {
    // TODO maps a database entity to a dto object -- there should be a domain entity

    public EnterpriseEntryDto map(final Optional<EnterpriseEntry> enterpriseEntry) {
        return enterpriseEntry.map(entry -> EnterpriseEntryDto.builder()
                .creationDate(entry.getCreationDate())
                .date(entry.getDate())
                .chargeabilityExternalEmployeesRecorded(
                        ProjectState.byName(
                                entry
                                        .getChargeabilityExternalEmployeesRecorded()
                                        .name()
                        )
                )
                .payrollAccountingSent(ProjectState.byName(entry.getPayrollAccountingSent().name()))
                .zepTimesReleased(ProjectState.byName(entry.getZepTimesReleased().name()))
                .build()).orElse(null);
    }
}
