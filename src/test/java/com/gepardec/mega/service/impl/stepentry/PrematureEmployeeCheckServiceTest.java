package com.gepardec.mega.service.impl.stepentry;

import com.gepardec.mega.db.repository.PrematureEmployeeCheckRepository;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.service.api.PrematureEmployeeCheckService;
import com.gepardec.mega.service.mapper.PrematureEmployeeCheckMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
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
    public void addPrematureEmployeeCheck_RETURN_TRUE(){
        PrematureEmployeeCheck prematureEmployeeCheck = PrematureEmployeeCheck.builder()
                .user(createUserForRole(Role.EMPLOYEE))
                .forMonth(LocalDate.of(2023, 10, 1))
                .build();

        when(userRepository.findActiveByEmail(any())).thenReturn(Optional.of(createDBUserForRole(Role.EMPLOYEE)));
        when(prematureEmployeeCheckRepository.save(any())).thenReturn(createDBPrematureEmployeeCheck(1L));

        assertThat(prematureEmployeeCheckService.addPrematureEmployeeCheck(prematureEmployeeCheck)).isTrue();
    }

    @Test
    public void addPrematureEmployeeCheck_RETURN_FALSE(){
        PrematureEmployeeCheck prematureEmployeeCheck = PrematureEmployeeCheck.builder()
                .user(createUserForRole(Role.EMPLOYEE))
                .forMonth(LocalDate.of(2023, 10, 1))
                .build();

        when(userRepository.findActiveByEmail(any())).thenReturn(Optional.of(createDBUserForRole(Role.EMPLOYEE)));
        when(prematureEmployeeCheckRepository.save(any())).thenReturn(createDBPrematureEmployeeCheck(null));

        assertThat(prematureEmployeeCheckService.addPrematureEmployeeCheck(prematureEmployeeCheck)).isFalse();
    }

    @Test
    public void getPrematureEmployeeCheckForEmail_RETURN_VALID(){
        when(prematureEmployeeCheckRepository.getFromEmail(any())).thenReturn(List.of(createDBPrematureEmployeeCheck(1L)));
        when(prematureEmployeeCheckMapper.mapListToDomain(any())).thenReturn(List.of(createPrematureEmployeeCheck()));

        List<PrematureEmployeeCheck> prematureEmployeeCheckForEmail = prematureEmployeeCheckService.getPrematureEmployeeCheckForEmail("max@mustermann.com");

        assertThat(prematureEmployeeCheckForEmail.size()).isEqualTo(1);
    }

    @Test
    public void getPrematureEmployeeCheckForEmail_RETURN_EMPTY(){
        when(prematureEmployeeCheckRepository.getFromEmail(any())).thenReturn(List.of());
        when(prematureEmployeeCheckMapper.mapListToDomain(any())).thenReturn(List.of());

        List<PrematureEmployeeCheck> prematureEmployeeCheckForEmail = prematureEmployeeCheckService.getPrematureEmployeeCheckForEmail("max@mustermann.com");

        assertThat(prematureEmployeeCheckForEmail.size()).isEqualTo(0);
    }

    @Test
    public void hasUerPrematureEmployeeCheck_RETURN_TRUE(){
        when(prematureEmployeeCheckMapper.mapListToDomain(any())).thenReturn(List.of(createPrematureEmployeeCheck()));

        boolean b = prematureEmployeeCheckService.hasUserPrematureEmployeeCheck("max@mustermann.com");
        assertThat(b).isTrue();
    }

    @Test
    public void hasUerPrematureEmployeeCheck_RETURN_FALSE(){
        when(prematureEmployeeCheckMapper.mapListToDomain(any())).thenReturn(List.of());

        boolean b = prematureEmployeeCheckService.hasUserPrematureEmployeeCheck("max@mustermann.com");
        assertThat(b).isFalse();
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
}
