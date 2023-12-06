package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.db.repository.PrematureEmployeeCheckRepository;
import com.gepardec.mega.db.repository.StepEntryRepository;
import com.gepardec.mega.domain.model.StepName;
import com.gepardec.mega.service.api.PrematureEmployeeCheckSyncService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@ApplicationScoped
public class PrematureEmployeeCheckSyncServiceImpl implements PrematureEmployeeCheckSyncService {

    @Inject
    PrematureEmployeeCheckRepository prematureEmployeeCheckRepository;

    @Inject
    StepEntryRepository stepEntryRepository;

    @Inject
    Logger logger;

    @Override
    public void syncPrematureEmployeeChecksWithStepEntries() {
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        List<PrematureEmployeeCheckEntity> prematureEmployeeCheckEntities = prematureEmployeeCheckRepository.findAllForMonth(currentMonth);

        logger.info(String.format("Syncing %s PrematureEmployeeChecks with StepEntries for Month: %s", prematureEmployeeCheckEntities.size(), currentMonth));

        for (PrematureEmployeeCheckEntity pec : prematureEmployeeCheckEntities) {
            Optional<StepEntry> optionalStepEntry = stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(currentMonth, pec.getUser()
                    .getEmail());

            try {
                StepEntry stepEntry = optionalStepEntry.orElseThrow();
                updateStepEntry(stepEntry, pec);
            } catch (NoSuchElementException e) {
                logger.error(String.format("Could not find StepEntry for PrematureEmployeeCheck! StepEntries or PrematureEmployeeChecks must be malformed! (PrematureEmployeeCheck ID: %s)", pec.getId()));
            }
        }
    }


    private void updateStepEntry(StepEntry stepEntry, PrematureEmployeeCheckEntity pec) {
        LocalDate startDate = pec.getForMonth().with(firstDayOfMonth());
        LocalDate endDate = pec.getForMonth().with(lastDayOfMonth());
        String ownerEmail = pec.getUser().getEmail();
        Long stepId = StepName.CONTROL_TIMES.getId();
        EmployeeState newState = EmployeeState.DONE;

        if (pec.getReason().isEmpty()) {
            stepEntryRepository.updateStateAssigned(startDate, endDate, ownerEmail, stepId, newState);
        } else {
            stepEntryRepository.updateStateAssignedWithReason(startDate, endDate, ownerEmail, stepId, newState, pec.getReason());
        }
    }
}
