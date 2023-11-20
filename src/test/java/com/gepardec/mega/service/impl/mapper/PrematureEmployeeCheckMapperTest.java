package com.gepardec.mega.service.impl.mapper;

import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.service.mapper.PrematureEmployeeCheckMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@QuarkusTest
public class PrematureEmployeeCheckMapperTest {

    @Inject
    PrematureEmployeeCheckMapper prematureEmployeeCheckMapper;


    @Test
    public void mapToDomain(){
        PrematureEmployeeCheck prematureEmployeeCheck = prematureEmployeeCheckMapper.mapToDomain(createDBPrematureEmployeeCheck(1L));

        assertAll(
                () -> assertThat(prematureEmployeeCheck.getId()).isEqualTo(1L),
                () -> assertThat(prematureEmployeeCheck.getForMonth()).isEqualTo(LocalDate.of(2023,10,1)),
                () -> assertThat(prematureEmployeeCheck.getUser().getClass()).isEqualTo(User.class)
        );
    }

    @Test
    public void mapListToDomain(){
        List<com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck> dbPrematureEmployeeCheck = List.of(createDBPrematureEmployeeCheck(1L), createDBPrematureEmployeeCheck(1L));
        List<PrematureEmployeeCheck> prematureEmployeeChecks = prematureEmployeeCheckMapper.mapListToDomain(dbPrematureEmployeeCheck);

        assertThat(prematureEmployeeChecks.size()).isEqualTo(2);
    }


    private com.gepardec.mega.db.entity.employee.User createDBUserForRole(final Role role) {
        com.gepardec.mega.db.entity.employee.User user = new com.gepardec.mega.db.entity.employee.User();
        user.setId(1L);
        user.setActive(true);
        user.setEmail("max@mustermann.com");
        return user;
    }

    private com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck createDBPrematureEmployeeCheck(Long id){
        com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck prematureEmployeeCheck = new com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck();
        prematureEmployeeCheck.setId(id);
        prematureEmployeeCheck.setUser(createDBUserForRole(Role.EMPLOYEE));
        prematureEmployeeCheck.setForMonth(LocalDate.of(2023, 10, 1));
        return prematureEmployeeCheck;
    }

}
