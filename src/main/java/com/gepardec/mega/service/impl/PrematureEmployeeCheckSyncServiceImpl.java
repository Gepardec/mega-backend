package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
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
    public boolean syncPrematureEmployeeChecksWithStepEntries(YearMonth payrollMonth) {
        boolean allEntriesUpdated = true;
        List<PrematureEmployeeCheck> prematureEmployeeCheckEntities = prematureEmployeeCheckService.findAllForMonth(payrollMonth)
                .stream()
                .filter(pec -> pec.getState().equals(PrematureEmployeeCheckState.DONE) || pec.getState().equals(PrematureEmployeeCheckState.IN_PROGRESS))
                .toList();

        logger.info("Syncing {} PrematureEmployeeChecks with StepEntries for Month: {}", prematureEmployeeCheckEntities.size(), payrollMonth);

        for (PrematureEmployeeCheck pec : prematureEmployeeCheckEntities) {
            boolean couldUpdatePec = updateStepEntry(pec) != 0;

            if (couldUpdatePec) {
                prematureEmployeeCheckService.deleteById(pec.getId());
            } else {
                logger.error("""
                                Could not find StepEntry for PrematureEmployeeCheck! \
                                StepEntries or PrematureEmployeeChecks must be malformed! \
                                (PrematureEmployeeCheck ID: {})""",
                        pec.getId()
                );
                allEntriesUpdated = false;
            }
        }

        prematureEmployeeCheckService.deleteAllForMonthWithState(payrollMonth, List.of(PrematureEmployeeCheckState.CANCELLED, PrematureEmployeeCheckState.IN_PROGRESS));

        return allEntriesUpdated;
    }

    private int updateStepEntry(PrematureEmployeeCheck pec) {
        LocalDate startDate = pec.getForMonth().with(firstDayOfMonth());
        LocalDate endDate = pec.getForMonth().with(lastDayOfMonth());
        String ownerEmail = pec.getUser().getEmail();
        Long stepId = StepName.CONTROL_TIMES.getId();

        if (StringUtils.isBlank(pec.getReason())) {
            return stepEntryRepository.updateStateAssigned(startDate, endDate, ownerEmail, stepId, EmployeeState.DONE);
        } else {
            return stepEntryRepository.updateStateAssignedWithReason(startDate, endDate, ownerEmail, stepId, EmployeeState.IN_PROGRESS, pec.getReason());
        }
    }
}
