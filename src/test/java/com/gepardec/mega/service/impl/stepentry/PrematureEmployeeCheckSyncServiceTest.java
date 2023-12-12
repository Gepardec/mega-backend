package com.gepardec.mega.service.impl.stepentry;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import com.gepardec.mega.db.entity.employee.Step;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.PrematureEmployeeCheckRepository;
import com.gepardec.mega.db.repository.StepEntryRepository;
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
    PrematureEmployeeCheckRepository prematureEmployeeCheckRepository;


    private final LocalDate testDate = LocalDate.of(2023, 11, 1);


    @Test
    void syncPrematureEmployeeChecksWithStepEntries_matchingStepEntryAndPrematureEmployeeCheck_invokeUpdateStepEntries1Time() {
//        Given
        List<PrematureEmployeeCheckEntity> prematureEmployeeCheckEntities = List.of(createPrematureEmployeeCheckEntity("test@test.com"));
        Optional<StepEntry> optionalStepEntry = Optional.of(createStepEntry());

        when(prematureEmployeeCheckRepository.findAllForMonth(any())).thenReturn(prematureEmployeeCheckEntities);
        when(stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(any(), any())).thenReturn(optionalStepEntry);

//        When
        boolean updatedAllEntries = prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(YearMonth.of(testDate.getYear(), testDate.getMonth()));

//        Then
        verify(stepEntryRepository, times(1)).updateStateAssigned(any(), any(), any(), any(), any());
        assertThat(updatedAllEntries).isTrue();
    }

    @Test
    void syncPrematureEmployeeChecksWithStepEntries_multipleMatchingStepEntryAndPrematureEmployeeCheck_invokeUpdateStepEntries3Times() {
//        Given
        List<PrematureEmployeeCheckEntity> prematureEmployeeCheckEntities = List.of(createPrematureEmployeeCheckEntity("test@test.com"), createPrematureEmployeeCheckEntity("test@test.com"), createPrematureEmployeeCheckEntity("test@test.com"));
        Optional<StepEntry> optionalStepEntry = Optional.of(createStepEntry());

        when(prematureEmployeeCheckRepository.findAllForMonth(any())).thenReturn(prematureEmployeeCheckEntities);
        when(stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(any(), any())).thenReturn(optionalStepEntry);

//        When
        boolean updatedAllEntries = prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(YearMonth.of(testDate.getYear(), testDate.getMonth()));

//        Then
        verify(stepEntryRepository, times(3)).updateStateAssigned(any(), any(), any(), any(), any());
        assertThat(updatedAllEntries).isTrue();
    }

    @Test
    void syncPrematureEmployeeChecksWithStepEntries_nonMatchingStepEntryAndPrematureEmployeeCheck_returnFalse() {
//        Given
        List<PrematureEmployeeCheckEntity> prematureEmployeeCheckEntities = List.of(createPrematureEmployeeCheckEntity("failing-test@test.com"));

        when(prematureEmployeeCheckRepository.findAllForMonth(any())).thenReturn(prematureEmployeeCheckEntities);
        when(stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(any(), any())).thenReturn(Optional.empty());

//        When
        boolean updatedAllEntries = prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(YearMonth.of(testDate.getYear(), testDate.getMonth()));

//        Then
        verify(stepEntryRepository, times(0)).updateStateAssigned(any(), any(), any(), any(), any());
        assertThat(updatedAllEntries).isFalse();
    }

    @Test
    void syncPrematureEmployeeChecksWithStepEntries_mishedMatchingStepEntryAndPrematureEmployeeCheck_returnFalseAndInvocate2Times() {
//        Given
        List<PrematureEmployeeCheckEntity> prematureEmployeeCheckEntities = List.of(createPrematureEmployeeCheckEntity("failing-test@test.com"), createPrematureEmployeeCheckEntity("test@test.com"), createPrematureEmployeeCheckEntity("test@test.com"));
        Optional<StepEntry> optionalStepEntry = Optional.of(createStepEntry());

        when(prematureEmployeeCheckRepository.findAllForMonth(any())).thenReturn(prematureEmployeeCheckEntities);
        when(stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(any(), any())).thenReturn(optionalStepEntry);
        when(stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(any(), eq("failing-test@test.com"))).thenReturn(Optional.empty());

//        When
        boolean updatedAllEntries = prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(YearMonth.of(testDate.getYear(), testDate.getMonth()));

//        Then
        verify(stepEntryRepository, times(2)).updateStateAssigned(any(), any(), any(), any(), any());
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


    private PrematureEmployeeCheckEntity createPrematureEmployeeCheckEntity(String email) {
        PrematureEmployeeCheckEntity prematureEmployeeCheckEntity = new PrematureEmployeeCheckEntity();
        User user = new User();
        user.setEmail(email);
        prematureEmployeeCheckEntity.setUser(user);
        prematureEmployeeCheckEntity.setForMonth(testDate);
        return prematureEmployeeCheckEntity;
    }

}
