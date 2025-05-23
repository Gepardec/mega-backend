package com.gepardec.mega.service.impl.enterpriseentry;

import com.gepardec.mega.db.entity.common.State;
import com.gepardec.mega.db.entity.enterprise.EnterpriseEntry;
import com.gepardec.mega.db.repository.EnterpriseEntryRepository;
import com.gepardec.mega.domain.model.ProjectState;
import com.gepardec.mega.rest.model.EnterpriseEntryDto;
import com.gepardec.mega.service.api.EnterpriseEntryService;
import com.gepardec.mega.service.mapper.EnterpriseEntryMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class EnterpriseEntryServiceImplTest {

    @InjectMock
    EnterpriseEntryRepository enterpriseEntryRepository;

    @InjectMock
    EnterpriseEntryMapper enterpriseEntryMapper;

    @Inject
    EnterpriseEntryService enterpriseEntryService;

    private YearMonth payrollMonth;
    private LocalDate fromDate;
    private LocalDate toDate;

    @BeforeEach
    void setUp() {
        payrollMonth = YearMonth.of(2023, 1);
        fromDate = LocalDate.of(2023, 1, 1);
        toDate = LocalDate.of(2023, 1, 31);
    }

    @Test
    void findByDate() {
        EnterpriseEntry entry = new EnterpriseEntry();
        EnterpriseEntryDto entryDto = EnterpriseEntryDto.builder().build();

        when(enterpriseEntryRepository.findByDate(fromDate, toDate))
                .thenReturn(Optional.of(entry));

        when(enterpriseEntryMapper.map(Optional.of(entry)))
                .thenReturn(entryDto);

        EnterpriseEntryDto result = enterpriseEntryService.findByDate(payrollMonth);

        assertThat(entryDto).isEqualTo(result);
        verify(enterpriseEntryRepository).findByDate(fromDate, toDate);
        verify(enterpriseEntryMapper).map(Optional.of(entry));
    }

    @Test
    void update_whenEntryExists_thenResultIsTrue() {
        EnterpriseEntry entry = new EnterpriseEntry();
        EnterpriseEntryDto entryDto = EnterpriseEntryDto.builder()
                .zepTimesReleased(ProjectState.DONE)
                .chargeabilityExternalEmployeesRecorded(ProjectState.WORK_IN_PROGRESS)
                .payrollAccountingSent(ProjectState.DONE)
                .build();

        when(enterpriseEntryRepository.findByDate(fromDate, toDate)).thenReturn(Optional.of(entry));
        when(enterpriseEntryRepository.updateEntry(any(EnterpriseEntry.class))).thenReturn(true);

        boolean result = enterpriseEntryService.update(entryDto, payrollMonth);

        assertThat(result).isTrue();
        verify(enterpriseEntryRepository).findByDate(fromDate, toDate);
        verify(enterpriseEntryRepository).updateEntry(entry);
        assertThat(State.DONE).isEqualTo(entry.getZepTimesReleased());
        assertThat(State.WORK_IN_PROGRESS).isEqualTo(entry.getChargeabilityExternalEmployeesRecorded());
        assertThat(State.DONE).isEqualTo(entry.getPayrollAccountingSent());
    }

    @Test
    void update_whenEntryDoesNotExist_thenResultIsFalse() {
        EnterpriseEntryDto entryDto = EnterpriseEntryDto.builder().build();

        when(enterpriseEntryRepository.findByDate(fromDate, toDate)).thenReturn(Optional.empty());

        boolean result = enterpriseEntryService.update(entryDto, payrollMonth);

        assertThat(result).isFalse();
        verify(enterpriseEntryRepository).findByDate(fromDate, toDate);
        verify(enterpriseEntryRepository, never()).updateEntry(any(EnterpriseEntry.class));
    }
}
