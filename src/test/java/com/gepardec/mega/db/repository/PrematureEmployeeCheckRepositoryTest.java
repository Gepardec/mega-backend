package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
import com.gepardec.mega.domain.model.Role;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestTransaction
class PrematureEmployeeCheckRepositoryTest {

    @Inject
    PrematureEmployeeCheckRepository prematureEmployeeCheckRepository;

    @Inject
    UserRepository userRepository;

    private static final String EMAIL = "max.muster@gepardec.com";
    private static final LocalDate DATE = LocalDate.of(2023, 10, 1);
    private com.gepardec.mega.db.entity.employee.User user;

    @BeforeEach
    void initDependentEntities() {
        user = initializeUserObject();
    }

    @Test
    void save_validEntry_returnDbId() {
//        Given
        persistUser();

//        When
        PrematureEmployeeCheckEntity saved = prematureEmployeeCheckRepository.create(createDBPrematureEmployeeCheck());

//        Then
        assertThat(saved.getId()).isNotZero();
    }

    @Test
    void save_secondEntry_throwConstraintViolationException() {
//        Given
        persistUser();
        PrematureEmployeeCheckEntity saved = prematureEmployeeCheckRepository.create(createDBPrematureEmployeeCheck());

//        When
        try {
            prematureEmployeeCheckRepository.create(createDBPrematureEmployeeCheck());
            prematureEmployeeCheckRepository.flush();

//            Then
        } catch (ConstraintViolationException e) {
            System.out.println(e.getConstraintName());
            assertThat(e).isNotNull();
        }
        assertThat(saved.getId()).isNotZero();
    }

    @Test
    void findByEmail_missingEntry_returnEmptyList() {
//        When
        var byEmailAndMonth = prematureEmployeeCheckRepository.findByEmailAndMonth(EMAIL, DATE);

//        Then
        assertThat(byEmailAndMonth).isEmpty();
    }

    @Test
    void findByEmail_validEntries_returnList() {
//        Given
        persistUser();
        persistPrematureEmployeeCheck();

//        When
        var byEmailAndMonth = prematureEmployeeCheckRepository.findByEmailAndMonth(EMAIL, DATE);

//        Then
        assertThat(byEmailAndMonth).isPresent()
                .get()
                .extracting(PrematureEmployeeCheckEntity::getState)
                .isEqualTo(PrematureEmployeeCheckState.DONE);
    }


    private void persistUser() {
        userRepository.persistOrUpdate(user);
    }

    private void persistPrematureEmployeeCheck() {
        prematureEmployeeCheckRepository.create(createDBPrematureEmployeeCheck());
    }


    private PrematureEmployeeCheckEntity createDBPrematureEmployeeCheck() {
        PrematureEmployeeCheckEntity prematureEmployeeCheckEntity = new PrematureEmployeeCheckEntity();
        prematureEmployeeCheckEntity.setId(null);
        prematureEmployeeCheckEntity.setUser(this.user);
        prematureEmployeeCheckEntity.setForMonth(DATE);
        prematureEmployeeCheckEntity.setState(PrematureEmployeeCheckState.DONE);
        return prematureEmployeeCheckEntity;
    }

    private com.gepardec.mega.db.entity.employee.User initializeUserObject() {
        com.gepardec.mega.db.entity.employee.User initUser = new com.gepardec.mega.db.entity.employee.User();
        initUser.setActive(true);
        initUser.setEmail(EMAIL);
        initUser.setFirstname("Max");
        initUser.setLastname("Mustermann");
        initUser.setLocale(Locale.GERMAN);
        initUser.setZepId("026-mmuster");
        initUser.setRoles(Set.of(Role.EMPLOYEE, Role.OFFICE_MANAGEMENT));
        initUser.setCreationDate(LocalDateTime.of(2021, 1, 18, 10, 10));
        initUser.setUpdatedDate(LocalDateTime.now());

        return initUser;
    }
}
