package com.gepardec.mega.service.impl.stepentry;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.PrematureEmployeeCheckRepository;
import com.gepardec.mega.db.repository.StepEntryRepository;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.service.api.PrematureEmployeeCheckSyncService;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@QuarkusTest
@TestTransaction
public class PrematureEmployeeCheckSyncServiceTest {

    @Inject
    PrematureEmployeeCheckSyncService prematureEmployeeCheckSyncService;

    @Inject
    StepEntryRepository stepEntryRepository;

    @Inject
    PrematureEmployeeCheckRepository prematureEmployeeCheckRepository;

    @Inject
    UserRepository userRepository;

    private final LocalDate testDate = LocalDate.of(2023, 11, 10);

    @Test
    public void syncPrematureEmployeeChecksWithStepEntries_matchingStepEntryAndPrematureEmployeeCheck_updateStepEntries(){
//        Given
        String testEmail = "test@test.com";
        persistUser(testEmail);
        persistStepEntry(testEmail);
        persistPrematureEmployeeCheck(testEmail);

//        When
        prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries();

//        Then

        Optional<StepEntry> optionalStepEntry = stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(testDate, testEmail);

        assertThat(optionalStepEntry.get().getState()).isEqualTo(EmployeeState.DONE);

    }



    private void persistStepEntry(String email){
        StepEntry stepEntry = createStepEntry(null, email);
        stepEntryRepository.persist(stepEntry);
    }

    private void persistPrematureEmployeeCheck(String email){
        PrematureEmployeeCheckEntity prematureEmployeeCheck = createPrematureEmployeeCheck(null, email);
        prematureEmployeeCheckRepository.save(prematureEmployeeCheck);
    }

    private void persistUser(String email){
        User user = createUser(email);
        userRepository.persistOrUpdate(user);
    }

    private StepEntry createStepEntry(Long id, String ownerEmail) {
        StepEntry stepEntry = new StepEntry();
        User user = createUser(ownerEmail);

        stepEntry.setId(id);
        stepEntry.setCreationDate(testDate.atStartOfDay());
        stepEntry.setDate(LocalDate.now());
        stepEntry.setProject("Liwest-EMS");
        stepEntry.setState(EmployeeState.OPEN);
        stepEntry.setUpdatedDate(testDate.atStartOfDay());

        stepEntry.setOwner(user);
        return stepEntry;
    }

    private User createUser(String ownerEmail){
        User user = new User();
        user.setEmail(ownerEmail);
        return user;
    }

    private PrematureEmployeeCheckEntity createPrematureEmployeeCheck(Long id, String ownerEmail) {
        PrematureEmployeeCheckEntity prematureEmployeeCheckEntity = new PrematureEmployeeCheckEntity();
        User user = createUser(ownerEmail);

        prematureEmployeeCheckEntity.setId(id);
        prematureEmployeeCheckEntity.setUser(user);
        prematureEmployeeCheckEntity.setForMonth(testDate);
        return prematureEmployeeCheckEntity;
    }

}
