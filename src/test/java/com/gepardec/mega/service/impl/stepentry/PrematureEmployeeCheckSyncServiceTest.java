package com.gepardec.mega.service.impl.stepentry;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
import com.gepardec.mega.db.entity.employee.Step;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.StepEntryRepository;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.service.api.PrematureEmployeeCheckService;
import com.gepardec.mega.service.api.PrematureEmployeeCheckSyncService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class PrematureEmployeeCheckSyncServiceTest {

    @Inject
    PrematureEmployeeCheckSyncService prematureEmployeeCheckSyncService;

    @InjectMock
    StepEntryRepository stepEntryRepository;

    @InjectMock
    PrematureEmployeeCheckService prematureEmployeeCheckService;

    private final YearMonth payrollMonth = YearMonth.of(2023, 11);

    @Test
    void syncPrematureEmployeeChecksWithStepEntries_matchingStepEntryAndPrematureEmployeeCheck_invokeUpdateStepEntries1Time() {
//        Given
        List<PrematureEmployeeCheck> prematureEmployeeCheckEntities = List.of(createPrematureEmployeeCheck(1, "test@test.com"));
        Optional<StepEntry> optionalStepEntry = Optional.of(createStepEntry());

        when(stepEntryRepository.updateStateAssigned(any(), any(), eq("test@test.com"), any(), any())).thenReturn(1);
        when(prematureEmployeeCheckService.findAllForMonth(any())).thenReturn(prematureEmployeeCheckEntities);
        when(stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(any(), any())).thenReturn(optionalStepEntry);

//        When
        boolean updatedAllEntries = prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(payrollMonth);

//        Then
        verify(stepEntryRepository, times(1)).updateStateAssigned(any(), any(), any(), any(), any());
        verify(prematureEmployeeCheckService, times(1)).deleteById(any());
        assertThat(updatedAllEntries).isTrue();
    }

    @Test
    void syncPrematureEmployeeChecksWithStepEntries_multipleMatchingStepEntryAndPrematureEmployeeCheck_invokeUpdateStepEntries3Times() {
//        Given
        List<PrematureEmployeeCheck> prematureEmployeeCheckEntities = List.of(
                createPrematureEmployeeCheck(1, "test@test.com"),
                createPrematureEmployeeCheck(1, "test@test.com"),
                createPrematureEmployeeCheck(1, "test@test.com")
        );
        Optional<StepEntry> optionalStepEntry = Optional.of(createStepEntry());

        when(prematureEmployeeCheckService.findAllForMonth(any())).thenReturn(prematureEmployeeCheckEntities);
        when(stepEntryRepository.updateStateAssigned(any(), any(), eq("test@test.com"), any(), any())).thenReturn(1);
        when(stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(any(), any())).thenReturn(optionalStepEntry);

//        When
        boolean updatedAllEntries = prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(payrollMonth);

//        Then
        verify(stepEntryRepository, times(3)).updateStateAssigned(any(), any(), any(), any(), any());
        verify(prematureEmployeeCheckService, times(3)).deleteById(any());
        assertThat(updatedAllEntries).isTrue();
    }

    @Test
    void syncPrematureEmployeeChecksWithStepEntries_nonMatchingStepEntryAndPrematureEmployeeCheck_returnFalse() {
//        Given
        List<PrematureEmployeeCheck> prematureEmployeeCheckEntities = List.of(createPrematureEmployeeCheck(-1, "failing-test@test.com"));

        when(prematureEmployeeCheckService.findAllForMonth(any())).thenReturn(prematureEmployeeCheckEntities);
        when(stepEntryRepository.updateStateAssigned(any(), any(), eq("failing-test@test.com"), any(), any())).thenReturn(0);
        when(stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(any(), any())).thenReturn(Optional.empty());

//        When
        boolean updatedAllEntries = prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(payrollMonth);

//        Then
        verify(stepEntryRepository, times(1)).updateStateAssigned(any(), any(), any(), any(), any());
        verify(prematureEmployeeCheckService, times(0)).deleteById(any());
        assertThat(updatedAllEntries).isFalse();
    }

    @Test
    void syncPrematureEmployeeChecksWithStepEntries_mishedMatchingStepEntryAndPrematureEmployeeCheck_returnFalseAndInvocate2Times() {
//        Given
        List<PrematureEmployeeCheck> prematureEmployeeCheckEntities = List.of(
                createPrematureEmployeeCheck(-1, "failing-test@test.com"),
                createPrematureEmployeeCheck(1, "test@test.com"),
                createPrematureEmployeeCheck(1, "test@test.com")
        );
        Optional<StepEntry> optionalStepEntry = Optional.of(createStepEntry());

        when(prematureEmployeeCheckService.findAllForMonth(any())).thenReturn(prematureEmployeeCheckEntities);
        when(stepEntryRepository.updateStateAssigned(any(), any(), eq("test@test.com"), any(), any())).thenReturn(1);
        when(stepEntryRepository.updateStateAssigned(any(), any(), eq("failing-test@test.com"), any(), any())).thenReturn(0);
        when(stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(any(), any())).thenReturn(optionalStepEntry);
        when(stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(any(), eq("failing-test@test.com"))).thenReturn(Optional.empty());

//        When
        boolean updatedAllEntries = prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(payrollMonth);

//        Then
        verify(stepEntryRepository, times(3)).updateStateAssigned(any(), any(), any(), any(), any());
        verify(prematureEmployeeCheckService, times(2)).deleteById(any());
        assertThat(updatedAllEntries).isFalse();
    }


    private StepEntry createStepEntry() {
        StepEntry stepEntry = new StepEntry();
        stepEntry.setCreationDate(payrollMonth.atDay(1).atStartOfDay());
        stepEntry.setDate(payrollMonth.atDay(1));
        stepEntry.setProject("Liwest-EMS");
        stepEntry.setState(EmployeeState.OPEN);
        stepEntry.setUpdatedDate(payrollMonth.atDay(1).atStartOfDay());
        stepEntry.setOwner(new User());
        stepEntry.setAssignee(new User());
        stepEntry.setStep(new Step());

        return stepEntry;
    }

    private PrematureEmployeeCheck createPrematureEmployeeCheck(long id, String email) {
        com.gepardec.mega.domain.model.User user = com.gepardec.mega.domain.model.User.builder().email(email).build();

        return PrematureEmployeeCheck.builder()
                .id(id)
                .user(user)
                .forMonth(payrollMonth.atDay(1))
                .state(PrematureEmployeeCheckState.DONE)
                .build();
    }
}
