package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.common.State;
import com.gepardec.mega.db.entity.enterprise.EnterpriseEntry;
import com.gepardec.mega.db.repository.EnterpriseEntryRepository;
import com.gepardec.mega.service.api.EnterpriseSyncService;
import com.gepardec.mega.service.api.ProjectService;
import com.gepardec.mega.service.api.UserService;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Dependent
@Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
public class EnterpriseSyncServiceImpl implements EnterpriseSyncService {

    @Inject
    Logger logger;

    @Inject
    UserService userService;

    @Inject
    ProjectService projectService;

    @Inject
    EnterpriseEntryRepository enterpriseEntryRepository;


    public boolean generateEnterpriseEntries(LocalDate date) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        logger.info("Started enterprise entry generation: {}", Instant.ofEpochMilli(stopWatch.getStartTime()));

        logger.info("Processing date: {}", date);

        Optional<EnterpriseEntry> savedEnterpriseEntry = enterpriseEntryRepository.findByDate(date);

        if (savedEnterpriseEntry.isEmpty()) {
            EnterpriseEntry enterpriseEntry = new EnterpriseEntry();
            enterpriseEntry.setDate(date);
            enterpriseEntry.setCreationDate(LocalDateTime.now());
            enterpriseEntry.setChargeabilityExternalEmployeesRecorded(State.OPEN);
            enterpriseEntry.setPayrollAccountingSent(State.OPEN);
            enterpriseEntry.setZepTimesReleased(State.OPEN);
            enterpriseEntry.setZepMonthlyReportDone(State.OPEN);

            enterpriseEntryRepository.persist(enterpriseEntry);
        } else {
            logger.debug("Enterprise entry for month {} already exists.", date.getMonth());
        }

        stopWatch.stop();

        logger.info("Enterprise entry generation took: {}ms", stopWatch.getTime());
        logger.info("Finished enterprise entry generation: {}", Instant.ofEpochMilli(stopWatch.getStartTime() + stopWatch.getTime()));

        return enterpriseEntryRepository.findByDate(date).isPresent();
    }

    @Override
    public boolean generateEnterpriseEntries() {
        return generateEnterpriseEntries(LocalDate.now().minusMonths(1).withDayOfMonth(1));
    }
}
