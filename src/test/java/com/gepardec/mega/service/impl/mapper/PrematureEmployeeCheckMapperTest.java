package com.gepardec.mega.service.impl.mapper;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import com.gepardec.mega.domain.mapper.PrematureEmployeeCheckMapper;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@QuarkusTest
public class PrematureEmployeeCheckMapperTest {

    @Inject
    PrematureEmployeeCheckMapper prematureEmployeeCheckMapper;


    @Test
    public void mapToDomain_dbEntity_mappedCorrectly() {
//        Given
        PrematureEmployeeCheckEntity dbPrematureEmployeeCheck = createDBPrematureEmployeeCheck(1L);

//        When
        PrematureEmployeeCheck prematureEmployeeCheck = prematureEmployeeCheckMapper.mapToDomain(dbPrematureEmployeeCheck);

//        Then
        assertAll(
                () -> assertThat(prematureEmployeeCheck.getId()).isEqualTo(1L).as("checkIdMappedCorrectly"),
                () -> assertThat(prematureEmployeeCheck.getForMonth()).isEqualTo(LocalDate.of(2023, 10, 1))
                        .as("checkDateMappedCorrectly"),
                () -> assertThat(prematureEmployeeCheck.getReason()).isEqualTo("reason")
                        .as("checkReasonMappedCorrectly"),
                () -> assertThat(prematureEmployeeCheck.getUser().getClass()).isEqualTo(User.class)
                        .as("checkClassMappedCorrectly")
        );
    }

    @Test
    public void mapListToDomain_dbEntityList_correctLength() {
//        Given
        List<PrematureEmployeeCheckEntity> prematureEmployeeCheckEntity = List.of(createDBPrematureEmployeeCheck(1L), createDBPrematureEmployeeCheck(1L));

//        When
        List<PrematureEmployeeCheck> prematureEmployeeChecks = prematureEmployeeCheckMapper.mapListToDomain(prematureEmployeeCheckEntity);

//        Then
        assertThat(prematureEmployeeChecks.size()).isEqualTo(2);
    }


    private com.gepardec.mega.db.entity.employee.User createDBUserForRole(final Role role) {
        com.gepardec.mega.db.entity.employee.User user = new com.gepardec.mega.db.entity.employee.User();
        user.setId(1L);
        user.setActive(true);
        user.setEmail("max@mustermann.com");
        return user;
    }

    private PrematureEmployeeCheckEntity createDBPrematureEmployeeCheck(Long id) {
        PrematureEmployeeCheckEntity prematureEmployeeCheckEntity = new PrematureEmployeeCheckEntity();
        prematureEmployeeCheckEntity.setId(id);
        prematureEmployeeCheckEntity.setUser(createDBUserForRole(Role.EMPLOYEE));
        prematureEmployeeCheckEntity.setForMonth(LocalDate.of(2023, 10, 1));
        prematureEmployeeCheckEntity.setReason("reason");
        return prematureEmployeeCheckEntity;
    }

}
