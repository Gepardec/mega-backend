package com.gepardec.mega.service.impl.prematureemployeecheck;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
import com.gepardec.mega.db.repository.PrematureEmployeeCheckRepository;
import com.gepardec.mega.domain.mapper.PrematureEmployeeCheckMapper;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.service.api.PrematureEmployeeCheckService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@QuarkusTest
class PrematureEmployeeCheckServiceImplTest {

    @Inject
    PrematureEmployeeCheckService prematureEmployeeCheckService;

    @InjectMock
    PrematureEmployeeCheckRepository checkRepository;

    @InjectMock
    PrematureEmployeeCheckMapper prematureEmployeeCheckMapper;

    @Test
    void findAllForMonth_whenSuccessful_thenReturnTrue() {
        List<PrematureEmployeeCheckEntity> entityList = new ArrayList<>();
        entityList.add(
                new PrematureEmployeeCheckEntity()
        );

        when(checkRepository.findAllForMonth(any(LocalDate.class)))
                .thenReturn(entityList);

        when(prematureEmployeeCheckMapper.mapListToDomain(any()))
                .thenReturn(List.of(PrematureEmployeeCheck.builder().id(1L).build()));

        List<PrematureEmployeeCheck> actual = prematureEmployeeCheckService.findAllForMonth(YearMonth.of(2025, 4));

        assertThat(actual.size()).isOne();
    }

    @Test
    void deleteById_whenSuccessful_thenReturnTrue() {
        when(checkRepository.delete(anyLong()))
                .thenReturn(true);

        assertThat(prematureEmployeeCheckService.deleteById(anyLong())).isTrue();
    }

    @Test
    void deleteAllForMonthWithState_whenSuccessful_thenReturnId() {
        when(checkRepository.deleteByMonthAndStates(any(LocalDate.class), any()))
                .thenReturn(1L);

        assertThat(prematureEmployeeCheckService.deleteAllForMonthWithState(YearMonth.of(2024, 3), List.of(PrematureEmployeeCheckState.IN_PROGRESS, PrematureEmployeeCheckState.DONE))).isEqualTo(1L);
    }

    @Test
    void update_whenSuccessful_thenReturnTrue() {
        PrematureEmployeeCheck prematureEmployeeCheck = PrematureEmployeeCheck.builder().id(1L)
                .forMonth(LocalDate.of(2024, 6, 1))
                .reason("Test reason")
                .build();

        when(checkRepository.findById(anyLong()))
                .thenReturn(createEntity());

        when(prematureEmployeeCheckMapper.mapToEntity(any(PrematureEmployeeCheck.class), any(PrematureEmployeeCheckEntity.class)))
                .thenReturn(createEntity());

        when(checkRepository.update(any(PrematureEmployeeCheckEntity.class)))
                .thenReturn(createEntity());

        boolean actual = prematureEmployeeCheckService.update(prematureEmployeeCheck);

        assertThat(actual).isTrue();
    }

    @Test
    void update_whenNotSuccessful_thenReturnFalse() {
        PrematureEmployeeCheck prematureEmployeeCheck = PrematureEmployeeCheck.builder().id(1L)
                .forMonth(LocalDate.of(2024, 6, 1))
                .reason("Test reason")
                .build();

        when(checkRepository.findById(anyLong()))
                .thenReturn(createEntity());

        when(prematureEmployeeCheckMapper.mapToEntity(any(PrematureEmployeeCheck.class), any(PrematureEmployeeCheckEntity.class)))
                .thenReturn(createEntity());

        when(checkRepository.update(any(PrematureEmployeeCheckEntity.class)))
                .thenReturn(createEntityWithIdNull());

        boolean actual = prematureEmployeeCheckService.update(prematureEmployeeCheck);

        assertThat(actual).isFalse();
    }

    private PrematureEmployeeCheckEntity createEntity() {
        PrematureEmployeeCheckEntity entity = new PrematureEmployeeCheckEntity();
        entity.setId(1L);
        entity.setForMonth(LocalDate.of(2024, 6, 1));
        entity.setReason("Test reason");

        return entity;
    }

    private PrematureEmployeeCheckEntity createEntityWithIdNull() {
        PrematureEmployeeCheckEntity entity = new PrematureEmployeeCheckEntity();
        entity.setId(null);
        entity.setForMonth(LocalDate.of(2024, 6, 1));
        entity.setReason("Test reason");

        return entity;
    }
}
