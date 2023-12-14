package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.repository.StepEntryRepository;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.domain.model.StepName;
import com.gepardec.mega.service.api.PrematureEmployeeCheckService;
import com.gepardec.mega.service.api.PrematureEmployeeCheckSyncService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@ApplicationScoped
public class PrematureEmployeeCheckSyncServiceImpl implements PrematureEmployeeCheckSyncService {

    @Inject
    PrematureEmployeeCheckService prematureEmployeeCheckService;

    @Inject
    StepEntryRepository stepEntryRepository;

    @Inject
    Logger logger;

    @Override
    public boolean syncPrematureEmployeeChecksWithStepEntries(YearMonth yearMonth) {
        boolean allEntriesUpdated = true;
        LocalDate selectedMonth = yearMonth.atDay(1);
        List<PrematureEmployeeCheck> prematureEmployeeCheckEntities = prematureEmployeeCheckService.findAllForMonth(selectedMonth);

        logger.info(
                String.format("Syncing %s PrematureEmployeeChecks with StepEntries for Month: %s",
                        prematureEmployeeCheckEntities.size(),
                        selectedMonth)
        );

        for (PrematureEmployeeCheck pec : prematureEmployeeCheckEntities) {
            try {
                updateStepEntry(pec);
            } catch (NoSuchElementException e) {
                logger.error(
                        String.format("Could not find StepEntry for PrematureEmployeeCheck! " +
                                        "StepEntries or PrematureEmployeeChecks must be malformed! " +
                                        "(PrematureEmployeeCheck ID: %s)",
                                pec.getId())
                );
                allEntriesUpdated = false;
            }
        }

        prematureEmployeeCheckService.deleteAllForMonth(selectedMonth);
        return allEntriesUpdated;
    }

    private void updateStepEntry(PrematureEmployeeCheck pec) {
        LocalDate startDate = pec.getForMonth().with(firstDayOfMonth());
        LocalDate endDate = pec.getForMonth().with(lastDayOfMonth());
        String ownerEmail = pec.getUser().getEmail();
        Long stepId = StepName.CONTROL_TIMES.getId();
        EmployeeState newState = EmployeeState.DONE;

        if (StringUtils.isBlank(pec.getReason())) {
            stepEntryRepository.updateStateAssigned(startDate, endDate, ownerEmail, stepId, newState);
        } else {
            stepEntryRepository.updateStateAssignedWithReason(startDate, endDate, ownerEmail, stepId, newState, pec.getReason());
        }
    }
}
