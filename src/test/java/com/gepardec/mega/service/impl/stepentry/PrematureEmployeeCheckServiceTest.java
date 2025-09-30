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
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@QuarkusTest
class PrematureEmployeeCheckServiceTest {

    @Inject
    PrematureEmployeeCheckService prematureEmployeeCheckService;

    @InjectMock
    private PrematureEmployeeCheckRepository prematureEmployeeCheckRepository;

    @InjectMock
    private PrematureEmployeeCheckMapper prematureEmployeeCheckMapper;

    @InjectMock
    private UserRepository userRepository;


    @Test
    void addPrematureEmployeeCheck_addValid_returnTrue() {
//        Given
        PrematureEmployeeCheck prematureEmployeeCheck = PrematureEmployeeCheck.builder()
                .user(createUserForRole())
                .forMonth(LocalDate.of(2023, 10, 1))
                .state(PrematureEmployeeCheckState.DONE)
                .build();

        when(userRepository.findActiveByEmail(any())).thenReturn(Optional.of(createDBUserForRole()));
        when(prematureEmployeeCheckRepository.create(any())).thenReturn(createDBPrematureEmployeeCheck(1L));
        when(prematureEmployeeCheckMapper.mapToEntity(any())).thenReturn(createDBPrematureEmployeeCheck(null));

//        When & Then
        assertThat(prematureEmployeeCheckService.create(prematureEmployeeCheck)).isTrue();
    }

    @Test
    void addPrematureEmployeeCheck_dbFails_returnFalse() {
//        Given
        PrematureEmployeeCheck prematureEmployeeCheck = PrematureEmployeeCheck.builder()
                .user(createUserForRole())
                .forMonth(LocalDate.of(2023, 10, 1))
                .state(PrematureEmployeeCheckState.DONE)
                .build();

        when(userRepository.findActiveByEmail(any())).thenReturn(Optional.of(createDBUserForRole()));
        when(prematureEmployeeCheckRepository.create(any())).thenReturn(createDBPrematureEmployeeCheck(null));
        when(prematureEmployeeCheckMapper.mapToEntity(any())).thenReturn(createDBPrematureEmployeeCheck(null));

//        When & Then
        assertThat(prematureEmployeeCheckService.create(prematureEmployeeCheck)).isFalse();
    }

    @Test
    void updatePrematureEmployeeCheck_validUpdate_returnTrue() {
        PrematureEmployeeCheck prematureEmployeeCheck = PrematureEmployeeCheck.builder()
                .id(1L)
                .user(createUserForRole())
                .forMonth(LocalDate.of(2023, 10, 1))
                .state(PrematureEmployeeCheckState.DONE)
                .build();

        PrematureEmployeeCheckEntity entity = createDBPrematureEmployeeCheck(1L);

        when(prematureEmployeeCheckRepository.findById(any())).thenReturn(entity);
        when(prematureEmployeeCheckMapper.mapToEntity(any(), any())).thenReturn(entity);
        when(prematureEmployeeCheckRepository.update(any())).thenReturn(entity);

        assertThat(prematureEmployeeCheckService.update(prematureEmployeeCheck)).isTrue();
    }

    @Test
    void updatePrematureEmployeeCheck_updateFails_returnFalse() {
        PrematureEmployeeCheck prematureEmployeeCheck = PrematureEmployeeCheck.builder()
                .id(1L)
                .user(createUserForRole())
                .forMonth(LocalDate.of(2023, 10, 1))
                .state(PrematureEmployeeCheckState.DONE)
                .build();

        PrematureEmployeeCheckEntity entity = createDBPrematureEmployeeCheck(null);

        when(prematureEmployeeCheckRepository.findById(any())).thenReturn(entity);
        when(prematureEmployeeCheckMapper.mapToEntity(any(), any())).thenReturn(entity);
        when(prematureEmployeeCheckRepository.update(any())).thenReturn(entity);

        assertThat(prematureEmployeeCheckService.update(prematureEmployeeCheck)).isFalse();
    }

    @Test
    void findAllForMonth_validMonth_returnList() {
        YearMonth payrollMonth = YearMonth.of(2023, 10);
        List<PrematureEmployeeCheckEntity> entityList = List.of(createDBPrematureEmployeeCheck(1L));
        List<PrematureEmployeeCheck> dtoList = List.of(PrematureEmployeeCheck.builder().build());

        when(prematureEmployeeCheckRepository.findAllForMonth(payrollMonth.atDay(1))).thenReturn(entityList);
        when(prematureEmployeeCheckMapper.mapListToDomain(any())).thenReturn(dtoList);

        assertThat(prematureEmployeeCheckService.findAllForMonth(payrollMonth)).isEqualTo(dtoList);
    }

    @Test
    void deleteAllForMonthWithState_validRequest_returnCount() {
        YearMonth payrollMonth = YearMonth.of(2023, 10);
        List<PrematureEmployeeCheckState> states = List.of(PrematureEmployeeCheckState.DONE);
        long expectedCount = 5L;

        when(prematureEmployeeCheckRepository.deleteByMonthAndStates(payrollMonth.atDay(1), states)).thenReturn(expectedCount);

        assertThat(prematureEmployeeCheckService.deleteAllForMonthWithState(payrollMonth, states)).isEqualTo(expectedCount);
    }

    @Test
    void deleteById_validId_returnTrue() {
        Long id = 1L;

        when(prematureEmployeeCheckRepository.delete(id)).thenReturn(true);

        assertThat(prematureEmployeeCheckService.deleteById(id)).isTrue();
    }

    @Test
    void deleteById_invalidId_returnFalse() {
        Long id = 1L;

        when(prematureEmployeeCheckRepository.delete(id)).thenReturn(false);

        assertThat(prematureEmployeeCheckService.deleteById(id)).isFalse();
    }

    private User createUserForRole() {
        return User.builder()
                .dbId(1)
                .userId("1")
                .email("max.mustermann@gpeardec.com")
                .firstname("Max")
                .lastname("Mustermann")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
    }

    private com.gepardec.mega.db.entity.employee.User createDBUserForRole() {
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
        prematureEmployeeCheckEntity.setUser(createDBUserForRole());
        prematureEmployeeCheckEntity.setForMonth(LocalDate.of(2023, 10, 1));
        prematureEmployeeCheckEntity.setState(PrematureEmployeeCheckState.DONE);
        return prematureEmployeeCheckEntity;
    }
}
