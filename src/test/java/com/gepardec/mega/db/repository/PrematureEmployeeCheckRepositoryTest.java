package com.gepardec.mega.db.repository;

import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@QuarkusTest
@TestTransaction
public class PrematureEmployeeCheckRepositoryTest {

    @Inject
    PrematureEmployeeCheckRepository prematureEmployeeCheckRepository;

    @Inject
    UserRepository userRepository;

    private static final String EMAIL = "max.muster@gepardec.com";
    private com.gepardec.mega.db.entity.employee.User user;

    @BeforeEach
    public void initDependentEntities(){
        user = initializeUserObject();
    }

    @Test
    public void addPrematureEmployeeCheck_RETURN_DB_ID(){
        persistUser();
        com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck saved = prematureEmployeeCheckRepository.save(createDBPrematureEmployeeCheck(null));
        assertThat(saved.getId()).isNotZero();
    }

    @Test
    public void addSecondPrematureEmployeeCheck_RETURN_VIOLATIONEXCEPTION(){
        persistUser();
        com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck saved = prematureEmployeeCheckRepository.save(createDBPrematureEmployeeCheck(null));
        try {
            com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck saved2 = prematureEmployeeCheckRepository.save(createDBPrematureEmployeeCheck(null));
            prematureEmployeeCheckRepository.flush();
        }catch (ConstraintViolationException e){
            System.out.println(e.getConstraintName());
            assertThat(e).isNotNull();
        }
        assertThat(saved.getId()).isNotZero();
    }

    @Test
    public void getFromEmail_RETURN_EMPTY(){
        List<com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck> fromEmail = prematureEmployeeCheckRepository.getFromEmail(EMAIL);
        assertThat(fromEmail.size()).isZero();
    }
    @Test
    public void getFromEmail_RETURN_CONTENT(){
        persistUser();
        persistPrematureEmployeeCheck();

        List<com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck> fromEmail = prematureEmployeeCheckRepository.getFromEmail(EMAIL);
        assertThat(fromEmail.size()).isEqualTo(1L);
    }


    private void persistUser(){
        userRepository.persistOrUpdate(user);
    }
    private void persistPrematureEmployeeCheck(){
        prematureEmployeeCheckRepository.save(createDBPrematureEmployeeCheck(null));
    }


    private com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck createDBPrematureEmployeeCheck(Long id){
        com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck prematureEmployeeCheck = new com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck();
        prematureEmployeeCheck.setId(id);
        prematureEmployeeCheck.setUser(this.user);
        prematureEmployeeCheck.setForMonth(LocalDate.of(2023, 10, 1));
        return prematureEmployeeCheck;
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
