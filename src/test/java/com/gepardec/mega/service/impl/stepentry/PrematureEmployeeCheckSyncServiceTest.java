package com.gepardec.mega.service.impl.stepentry;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.Step;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.StepEntryRepository;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.service.api.PrematureEmployeeCheckService;
import com.gepardec.mega.service.api.PrematureEmployeeCheckSyncService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
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
public class PrematureEmployeeCheckSyncServiceTest {

    @Inject
    PrematureEmployeeCheckSyncService prematureEmployeeCheckSyncService;

    @InjectMock
    StepEntryRepository stepEntryRepository;

    @InjectMock
    PrematureEmployeeCheckService prematureEmployeeCheckService;

    private final LocalDate testDate = LocalDate.of(2023, 11, 1);

    @Test
    void syncPrematureEmployeeChecksWithStepEntries_matchingStepEntryAndPrematureEmployeeCheck_invokeUpdateStepEntries1Time() {
//        Given
        List<PrematureEmployeeCheck> prematureEmployeeCheckEntities = List.of(createPrematureEmployeeCheck("test@test.com"));
        Optional<StepEntry> optionalStepEntry = Optional.of(createStepEntry());

        when(prematureEmployeeCheckService.findAllForMonth(any())).thenReturn(prematureEmployeeCheckEntities);
        when(stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(any(), any())).thenReturn(optionalStepEntry);

//        When
        boolean updatedAllEntries = prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(
                YearMonth.of(testDate.getYear(), testDate.getMonth())
        );

//        Then
        verify(stepEntryRepository, times(1)).updateStateAssigned(any(), any(), any(), any(), any());
        verify(prematureEmployeeCheckService, times(1)).deleteAllForMonth(any());
        assertThat(updatedAllEntries).isTrue();
    }

    @Test
    void syncPrematureEmployeeChecksWithStepEntries_multipleMatchingStepEntryAndPrematureEmployeeCheck_invokeUpdateStepEntries3Times() {
//        Given
        List<PrematureEmployeeCheck> prematureEmployeeCheckEntities = List.of(
                createPrematureEmployeeCheck("test@test.com"),
                createPrematureEmployeeCheck("test@test.com"),
                createPrematureEmployeeCheck("test@test.com")
        );
        Optional<StepEntry> optionalStepEntry = Optional.of(createStepEntry());

        when(prematureEmployeeCheckService.findAllForMonth(any())).thenReturn(prematureEmployeeCheckEntities);
        when(stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(any(), any())).thenReturn(optionalStepEntry);

//        When
        boolean updatedAllEntries = prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(
                YearMonth.of(testDate.getYear(), testDate.getMonth())
        );

//        Then
        verify(stepEntryRepository, times(3)).updateStateAssigned(any(), any(), any(), any(), any());
        verify(prematureEmployeeCheckService, times(1)).deleteAllForMonth(any());
        assertThat(updatedAllEntries).isTrue();
    }

    @Test
    void syncPrematureEmployeeChecksWithStepEntries_nonMatchingStepEntryAndPrematureEmployeeCheck_returnFalse() {
//        Given
        List<PrematureEmployeeCheck> prematureEmployeeCheckEntities = List.of(createPrematureEmployeeCheck("failing-test@test.com"));

        when(prematureEmployeeCheckService.findAllForMonth(any())).thenReturn(prematureEmployeeCheckEntities);
        when(stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(any(), any())).thenReturn(Optional.empty());

//        When
        boolean updatedAllEntries = prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(
                YearMonth.of(testDate.getYear(), testDate.getMonth())
        );

//        Then
        verify(stepEntryRepository, times(0)).updateStateAssigned(any(), any(), any(), any(), any());
        verify(prematureEmployeeCheckService, times(1)).deleteAllForMonth(any());
        assertThat(updatedAllEntries).isFalse();
    }

    @Test
    void syncPrematureEmployeeChecksWithStepEntries_mishedMatchingStepEntryAndPrematureEmployeeCheck_returnFalseAndInvocate2Times() {
//        Given
        List<PrematureEmployeeCheck> prematureEmployeeCheckEntities = List.of(
                createPrematureEmployeeCheck("failing-test@test.com"),
                createPrematureEmployeeCheck("test@test.com"),
                createPrematureEmployeeCheck("test@test.com")
        );
        Optional<StepEntry> optionalStepEntry = Optional.of(createStepEntry());

        when(prematureEmployeeCheckService.findAllForMonth(any())).thenReturn(prematureEmployeeCheckEntities);
        when(stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(any(), any())).thenReturn(optionalStepEntry);
        when(stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(any(), eq("failing-test@test.com"))).thenReturn(Optional.empty());

//        When
        boolean updatedAllEntries = prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(
                YearMonth.of(testDate.getYear(), testDate.getMonth())
        );

//        Then
        verify(stepEntryRepository, times(2)).updateStateAssigned(any(), any(), any(), any(), any());
        verify(prematureEmployeeCheckService, times(1)).deleteAllForMonth(any());
        assertThat(updatedAllEntries).isFalse();
    }


    private StepEntry createStepEntry() {
        StepEntry stepEntry = new StepEntry();
        stepEntry.setCreationDate(testDate.atStartOfDay());
        stepEntry.setDate(testDate);
        stepEntry.setProject("Liwest-EMS");
        stepEntry.setState(EmployeeState.OPEN);
        stepEntry.setUpdatedDate(testDate.atStartOfDay());
        stepEntry.setOwner(new User());
        stepEntry.setAssignee(new User());
        stepEntry.setStep(new Step());

        return stepEntry;
    }

    private PrematureEmployeeCheck createPrematureEmployeeCheck(String email) {
        com.gepardec.mega.domain.model.User user = new com.gepardec.mega.domain.model.User();
        user.setEmail(email);

        return PrematureEmployeeCheck.builder()
                .user(user)
                .forMonth(testDate)
                .build();
    }
}
