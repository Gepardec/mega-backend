package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.common.State;
import com.gepardec.mega.db.entity.enterprise.EnterpriseEntryEntity;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@QuarkusTest
@TestTransaction
class EnterpriseEntryRepositoryTest {

    @Inject
    EnterpriseEntryRepository enterpriseEntryRepository;

    @Test
    void generateEnterpriseEntry() {
        EnterpriseEntryEntity enterpriseEntry = new EnterpriseEntryEntity();
        enterpriseEntry.setDate(LocalDate.now());
        enterpriseEntry.setCreationDate(LocalDateTime.now());
        enterpriseEntry.setChargeabilityExternalEmployeesRecorded(State.OPEN);
        enterpriseEntry.setPayrollAccountingSent(State.OPEN);
        enterpriseEntry.setZepTimesReleased(State.OPEN);
        enterpriseEntry.setZepMonthlyReportDone(State.OPEN);

        enterpriseEntryRepository.persist(enterpriseEntry);

        Optional<EnterpriseEntryEntity> enterpriseEntryValue = enterpriseEntryRepository.findByDate(LocalDate.now());

        assertAll(
                () -> assertThat(enterpriseEntryValue).isPresent(),
                () -> assertThat(enterpriseEntryValue.get().getZepTimesReleased()).isEqualTo(State.OPEN)
        );
    }
}
