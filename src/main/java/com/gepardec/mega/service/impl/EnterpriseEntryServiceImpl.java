package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.common.State;
import com.gepardec.mega.db.entity.enterprise.EnterpriseEntry;
import com.gepardec.mega.db.repository.EnterpriseEntryRepository;
import com.gepardec.mega.rest.model.EnterpriseEntryDto;
import com.gepardec.mega.service.api.EnterpriseEntryService;
import com.gepardec.mega.service.mapper.EnterpriseEntryMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.Optional;

@ApplicationScoped
public class EnterpriseEntryServiceImpl implements EnterpriseEntryService {

    @Inject
    EnterpriseEntryMapper enterpriseEntryMapper;

    @Inject
    EnterpriseEntryRepository enterpriseEntryRepository;

    @Override
    public EnterpriseEntryDto findByDate(YearMonth payrollMonth) {
        return enterpriseEntryMapper.map(
                enterpriseEntryRepository.findByDate(payrollMonth.atDay(1), payrollMonth.atEndOfMonth())
        );
    }

    @Override
    public boolean update(EnterpriseEntryDto updatedEntryDto, YearMonth payrollMonth) {
        Optional<EnterpriseEntry> optionalEntry = enterpriseEntryRepository.findByDate(payrollMonth.atDay(1), payrollMonth.atEndOfMonth());

        if (optionalEntry.isPresent()) {
            EnterpriseEntry entry = optionalEntry.get();
            entry.setChargeabilityExternalEmployeesRecorded(
                    State.valueOf(updatedEntryDto.getChargeabilityExternalEmployeesRecorded().name())
            );
            entry.setPayrollAccountingSent(State.valueOf(updatedEntryDto.getPayrollAccountingSent().name()));
            entry.setZepTimesReleased(State.valueOf(updatedEntryDto.getZepTimesReleased().name()));

            return enterpriseEntryRepository.updateEntry(entry);
        }
        return false;
    }
}
