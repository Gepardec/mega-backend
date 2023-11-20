package com.gepardec.mega.db.repository;

import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@QuarkusTest
@TestTransaction
public class PrematureEmployeeCheckRepositoryTest {

    @Inject
    PrematureEmployeeCheckRepository prematureEmployeeCheckRepository;


    @Test
    public void addPrematureEmployeeCheck(){
        com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck saved = prematureEmployeeCheckRepository.save(createDBPrematureEmployeeCheck(null));
        assertThat(saved.getId()).isNotZero();
    }

    private com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck createDBPrematureEmployeeCheck(Long id){
        com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck prematureEmployeeCheck = new com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck();
        prematureEmployeeCheck.setId(id);
        prematureEmployeeCheck.setUser(createDBUserForRole(Role.EMPLOYEE));
        prematureEmployeeCheck.setForMonth(LocalDate.of(2023, 10, 1));
        return prematureEmployeeCheck;
    }

    private PrematureEmployeeCheck createPrematureEmployeeCheck(){
        return PrematureEmployeeCheck.builder()
                .id(1)
                .creationDate(LocalDateTime.now())
                .user(createUserForRole(Role.EMPLOYEE))
                .forMonth(LocalDate.of(2023,10,1))
                .build();
    }

    private User createUserForRole(final Role role) {
        return User.builder()
                .dbId(1)
                .userId("1")
                .email("max.mustermann@gpeardec.com")
                .firstname("Max")
                .lastname("Mustermann")
                .roles(Set.of(role))
                .build();
    }

    private com.gepardec.mega.db.entity.employee.User createDBUserForRole(final Role role) {
        com.gepardec.mega.db.entity.employee.User user = new com.gepardec.mega.db.entity.employee.User();
        user.setId(1L);
        user.setActive(true);
        user.setEmail("max@mustermann.com");
//      Nothing else is needed
        return user;
    }
}
