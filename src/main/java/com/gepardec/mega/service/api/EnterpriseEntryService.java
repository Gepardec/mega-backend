package com.gepardec.mega.service.api;

import com.gepardec.mega.rest.model.EnterpriseEntryDto;

import java.time.YearMonth;

public interface EnterpriseEntryService {

    EnterpriseEntryDto findByDate(final YearMonth payrollMonth);

    boolean update(final EnterpriseEntryDto updatedEntryDto, final YearMonth payrollMonth);
}
