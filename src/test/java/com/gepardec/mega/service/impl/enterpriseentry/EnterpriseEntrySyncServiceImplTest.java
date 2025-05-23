package com.gepardec.mega.service.impl.enterpriseentry;

import com.gepardec.mega.db.entity.common.State;
import com.gepardec.mega.db.entity.enterprise.EnterpriseEntry;
import com.gepardec.mega.db.repository.EnterpriseEntryRepository;
import com.gepardec.mega.service.api.EnterpriseSyncService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class EnterpriseEntrySyncServiceImplTest {

    @InjectMock
    EnterpriseEntryRepository enterpriseEntryRepository;

    @Inject
    EnterpriseSyncService enterpriseSyncService;

    @InjectMock
    Logger logger;


    private EnterpriseEntry enterpriseEntry;
    private YearMonth payrollMonth;

    @BeforeEach
    void setUp() {
        payrollMonth = YearMonth.of(2023, 10);
        enterpriseEntry = new EnterpriseEntry();
        enterpriseEntry.setDate(payrollMonth.atDay(1));
        enterpriseEntry.setCreationDate(LocalDateTime.now());
        enterpriseEntry.setChargeabilityExternalEmployeesRecorded(State.OPEN);
        enterpriseEntry.setPayrollAccountingSent(State.OPEN);
        enterpriseEntry.setZepTimesReleased(State.OPEN);
        enterpriseEntry.setZepMonthlyReportDone(State.OPEN);
    }

    @Test
    void generateEnterpriseEntries_noExistingEntry_createsEntry() {
        // returns Optional empty when reached for the first time, Optional.of() any other time
        when(enterpriseEntryRepository.findByDate(any(LocalDate.class)))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(enterpriseEntry));


        boolean result = enterpriseSyncService.generateEnterpriseEntries(payrollMonth);

        ArgumentCaptor<EnterpriseEntry> captor = ArgumentCaptor.forClass(EnterpriseEntry.class);
        verify(enterpriseEntryRepository).persist(captor.capture());
        EnterpriseEntry persistedEntry = captor.getValue();

        assertThat(persistedEntry.getDate()).isEqualTo(payrollMonth.atDay(1));
        assertThat(persistedEntry.getChargeabilityExternalEmployeesRecorded()).isEqualTo(State.OPEN);
        assertThat(persistedEntry.getPayrollAccountingSent()).isEqualTo(State.OPEN);
        assertThat(persistedEntry.getZepTimesReleased()).isEqualTo(State.OPEN);
        assertThat(persistedEntry.getZepMonthlyReportDone()).isEqualTo(State.OPEN);

        verify(enterpriseEntryRepository, times(2)).findByDate(any(LocalDate.class));
        verify(logger, times(2)).info(anyString(), any(Instant.class));
        verify(logger, times(1)).info(eq("Processing date: {}"), eq(payrollMonth));

        assertThat(result).isTrue();
    }

    @Test
    void generateEnterpriseEntries_existingEntry_logsDebugMessage() {
        when(enterpriseEntryRepository.findByDate(any(LocalDate.class))).thenReturn(Optional.of(enterpriseEntry));

        boolean result = enterpriseSyncService.generateEnterpriseEntries(payrollMonth);

        verify(enterpriseEntryRepository, never()).persist(any(EnterpriseEntry.class));
        verify(logger, times(1)).debug(eq("Enterprise entry for month {} already exists."), eq(payrollMonth.getMonth()));
        verify(logger, times(1)).info(eq("Started enterprise entry generation: {}"), any(Instant.class));
        verify(logger, times(1)).info(eq("Finished enterprise entry generation: {}"), any(Instant.class));
        verify(logger, times(1)).info(eq("Processing date: {}"), eq(payrollMonth));
        verify(logger, times(1)).info(eq("Enterprise entry generation took: {}ms"), anyLong());

        assertThat(result).isTrue();
    }
}
