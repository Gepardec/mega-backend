package com.gepardec.mega.service.impl.mapper;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
import com.gepardec.mega.domain.mapper.PrematureEmployeeCheckMapper;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.domain.model.User;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@QuarkusTest
class PrematureEmployeeCheckMapperTest {

    @Inject
    PrematureEmployeeCheckMapper prematureEmployeeCheckMapper;


    @Test
    void mapToDomain_dbEntity_mappedCorrectly() {
//        Given
        PrematureEmployeeCheckEntity dbPrematureEmployeeCheck = createDBPrematureEmployeeCheck();

//        When
        PrematureEmployeeCheck prematureEmployeeCheck = prematureEmployeeCheckMapper.mapToDomain(dbPrematureEmployeeCheck);

//        Then
        assertAll(
                () -> assertThat(prematureEmployeeCheck.getId()).isEqualTo(1L),
                () -> assertThat(prematureEmployeeCheck.getForMonth()).isEqualTo(LocalDate.of(2023, 10, 1))
                        .as("checkDateMappedCorrectly"),
                () -> assertThat(prematureEmployeeCheck.getReason()).isEqualTo("reason")
                        .as("checkReasonMappedCorrectly"),
                () -> assertThat(prematureEmployeeCheck.getUser().getClass()).isEqualTo(User.class)
                        .as("checkClassMappedCorrectly")
        );
    }

    @Test
    void mapListToDomain_dbEntityList_correctLength() {
//        Given
        List<PrematureEmployeeCheckEntity> prematureEmployeeCheckEntity = List.of(createDBPrematureEmployeeCheck(), createDBPrematureEmployeeCheck());

//        When
        List<PrematureEmployeeCheck> prematureEmployeeChecks = prematureEmployeeCheckMapper.mapListToDomain(prematureEmployeeCheckEntity);

//        Then
        assertThat(prematureEmployeeChecks).hasSize(2);
    }

    @Test
    void mapToEntity() {
        PrematureEmployeeCheck domain = createPrematureEmployeeCheck(User.builder().userId("001-maxmustermann").build(), "Test reason");
        PrematureEmployeeCheckEntity actual = prematureEmployeeCheckMapper
                .mapToEntity(domain);

        assertThat(actual.getState()).isEqualTo(domain.getState());
        assertThat(actual.getReason()).isEqualTo(domain.getReason());
        assertThat(actual.getId()).isEqualTo(domain.getId());
    }

    @Test
    void mapToEntityWithTwoParams_whenReasonNotNull() {
        PrematureEmployeeCheck domain = createPrematureEmployeeCheck(User.builder().userId("001-maxmustermann").build(), "Test reason");
        PrematureEmployeeCheckEntity entity = new PrematureEmployeeCheckEntity();
        entity.setId(domain.getId());

        PrematureEmployeeCheckEntity actual = prematureEmployeeCheckMapper.mapToEntity(domain, entity);

        assertThat(actual.getState()).isEqualTo(domain.getState());
        assertThat(actual.getReason()).isEqualTo(domain.getReason());
        assertThat(actual.getForMonth()).isEqualTo(domain.getForMonth());
    }

    @Test
    void mapToEntity_WithTwoParams_whenReasonIsNull() {
        PrematureEmployeeCheck domain = createPrematureEmployeeCheck(User.builder().userId("001-maxmustermann").build(), null);
        PrematureEmployeeCheckEntity entity = new PrematureEmployeeCheckEntity();
        entity.setId(domain.getId());

        PrematureEmployeeCheckEntity actual = prematureEmployeeCheckMapper.mapToEntity(domain, entity);

        assertThat(actual.getState()).isEqualTo(domain.getState());
        assertThat(actual.getReason()).isEqualTo(domain.getReason());
        assertThat(actual.getForMonth()).isEqualTo(domain.getForMonth());
    }


    private com.gepardec.mega.db.entity.employee.User createDBUserForRole() {
        com.gepardec.mega.db.entity.employee.User user = new com.gepardec.mega.db.entity.employee.User();
        user.setId(1L);
        user.setActive(true);
        user.setEmail("max@mustermann.com");
        return user;
    }

    private PrematureEmployeeCheck createPrematureEmployeeCheck(User user, String reason) {
        return PrematureEmployeeCheck.builder()
                .id(1L)
                .user(user)
                .forMonth(LocalDate.of(2024, 6, 1))
                .reason(reason)
                .state(PrematureEmployeeCheckState.IN_PROGRESS)
                .build();
    }

    private PrematureEmployeeCheckEntity createDBPrematureEmployeeCheck() {
        PrematureEmployeeCheckEntity prematureEmployeeCheckEntity = new PrematureEmployeeCheckEntity();
        prematureEmployeeCheckEntity.setId(1L);
        prematureEmployeeCheckEntity.setUser(createDBUserForRole());
        prematureEmployeeCheckEntity.setForMonth(LocalDate.of(2023, 10, 1));
        prematureEmployeeCheckEntity.setReason("reason");
        return prematureEmployeeCheckEntity;
    }
}
