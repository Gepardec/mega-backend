package com.gepardec.mega.service.impl.stepentry;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
import com.gepardec.mega.db.repository.PrematureEmployeeCheckRepository;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.domain.mapper.PrematureEmployeeCheckMapper;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.service.api.PrematureEmployeeCheckService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@QuarkusTest
public class PrematureEmployeeCheckServiceTest {
    @Inject
    PrematureEmployeeCheckService prematureEmployeeCheckService;

    @InjectMock
    private PrematureEmployeeCheckRepository prematureEmployeeCheckRepository;

    @InjectMock
    private PrematureEmployeeCheckMapper prematureEmployeeCheckMapper;

    @InjectMock
    private UserRepository userRepository;


    @Test
    public void addPrematureEmployeeCheck_addValid_returnTrue() {
//        Given
        PrematureEmployeeCheck prematureEmployeeCheck = PrematureEmployeeCheck.builder()
                .user(createUserForRole(Role.EMPLOYEE))
                .forMonth(LocalDate.of(2023, 10, 1))
                .state(PrematureEmployeeCheckState.DONE)
                .build();

        when(userRepository.findActiveByEmail(any())).thenReturn(Optional.of(createDBUserForRole(Role.EMPLOYEE)));
        when(prematureEmployeeCheckRepository.create(any())).thenReturn(createDBPrematureEmployeeCheck(1L));
        when(prematureEmployeeCheckMapper.mapToEntity(any())).thenReturn(createDBPrematureEmployeeCheck(null));

//        When & Then
        assertThat(prematureEmployeeCheckService.create(prematureEmployeeCheck)).isTrue();
    }

    @Test
    public void addPrematureEmployeeCheck_dbFails_returnFalse() {
//        Given
        PrematureEmployeeCheck prematureEmployeeCheck = PrematureEmployeeCheck.builder()
                .user(createUserForRole(Role.EMPLOYEE))
                .forMonth(LocalDate.of(2023, 10, 1))
                .state(PrematureEmployeeCheckState.DONE)
                .build();

        when(userRepository.findActiveByEmail(any())).thenReturn(Optional.of(createDBUserForRole(Role.EMPLOYEE)));
        when(prematureEmployeeCheckRepository.create(any())).thenReturn(createDBPrematureEmployeeCheck(null));
        when(prematureEmployeeCheckMapper.mapToEntity(any())).thenReturn(createDBPrematureEmployeeCheck(null));

//        When & Then
        assertThat(prematureEmployeeCheckService.create(prematureEmployeeCheck)).isFalse();
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

    private PrematureEmployeeCheckEntity createDBPrematureEmployeeCheck(Long id) {
        PrematureEmployeeCheckEntity prematureEmployeeCheckEntity = new PrematureEmployeeCheckEntity();
        prematureEmployeeCheckEntity.setId(id);
        prematureEmployeeCheckEntity.setUser(createDBUserForRole(Role.EMPLOYEE));
        prematureEmployeeCheckEntity.setForMonth(LocalDate.of(2023, 10, 1));
        prematureEmployeeCheckEntity.setState(PrematureEmployeeCheckState.DONE);
        return prematureEmployeeCheckEntity;
    }
}
