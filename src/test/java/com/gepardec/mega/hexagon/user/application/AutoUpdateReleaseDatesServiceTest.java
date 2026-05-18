package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.application.port.inbound.AutoUpdateReleaseDatesResult;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.PayrollMonthCompletionPort;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.user.domain.port.outbound.ZepEmployeePort;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoUpdateReleaseDatesServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    ZepEmployeePort zepEmployeePort;

    @Mock
    PayrollMonthCompletionPort payrollMonthCompletionPort;

    AutoUpdateReleaseDatesService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-05-19T08:00:00Z"), ZoneOffset.UTC);
        service = new AutoUpdateReleaseDatesService(
                userRepository,
                zepEmployeePort,
                payrollMonthCompletionPort,
                clock
        );
    }

    @Test
    void autoUpdate_shouldComputePreviousPayrollMonthAndProcessEmployeesIndependently() {
        YearMonth payrollMonth = YearMonth.of(2026, 4);
        LocalDate releaseDate = LocalDate.of(2026, 4, 30);

        User successfulUser = user("success", LocalDate.of(2025, 1, 1), null);
        User failingUser = user("failure", LocalDate.of(2025, 1, 1), null);
        User inactiveUser = user("inactive", LocalDate.of(2024, 1, 1), LocalDate.of(2026, 3, 31));

        when(userRepository.findAll()).thenReturn(List.of(successfulUser, failingUser, inactiveUser));
        when(payrollMonthCompletionPort.findUsersWithAllTasksCompleted(payrollMonth))
                .thenReturn(Set.of(successfulUser.id(), failingUser.id(), inactiveUser.id()));
        when(zepEmployeePort.updateReleaseDate(successfulUser.zepUsername(), releaseDate))
                .thenReturn(Uni.createFrom().voidItem());
        when(zepEmployeePort.updateReleaseDate(failingUser.zepUsername(), releaseDate))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("zep write failed")));

        AutoUpdateReleaseDatesResult result = service.autoUpdate();

        assertThat(result.payrollMonth()).isEqualTo(payrollMonth);
        assertThat(result.releaseDate()).isEqualTo(releaseDate);
        assertThat(result.updatedUserIds()).containsExactly(successfulUser.id());
        assertThat(result.failedUserIds()).containsExactly(failingUser.id());

        verify(userRepository).saveAll(List.of(successfulUser.withReleaseDate(releaseDate)));
    }

    @Test
    void autoUpdate_shouldMarkEmployeeAsFailedWhenLocalPersistenceFailsAfterSuccessfulZepUpdate() {
        YearMonth payrollMonth = YearMonth.of(2026, 4);
        LocalDate releaseDate = LocalDate.of(2026, 4, 30);

        User localFailUser = user("local-fail", LocalDate.of(2025, 1, 1), null);
        User persistedUser = user("persisted", LocalDate.of(2025, 1, 1), null);

        when(userRepository.findAll()).thenReturn(List.of(localFailUser, persistedUser));
        when(payrollMonthCompletionPort.findUsersWithAllTasksCompleted(payrollMonth))
                .thenReturn(Set.of(localFailUser.id(), persistedUser.id()));
        when(zepEmployeePort.updateReleaseDate(localFailUser.zepUsername(), releaseDate))
                .thenReturn(Uni.createFrom().voidItem());
        when(zepEmployeePort.updateReleaseDate(persistedUser.zepUsername(), releaseDate))
                .thenReturn(Uni.createFrom().voidItem());

        doAnswer(invocation -> {
            List<User> users = invocation.getArgument(0);
            if (users.getFirst().id().equals(localFailUser.id())) {
                throw new RuntimeException("db error");
            }
            return null;
        }).when(userRepository).saveAll(anyList());

        AutoUpdateReleaseDatesResult result = service.autoUpdate();

        assertThat(result.updatedUserIds()).containsExactly(persistedUser.id());
        assertThat(result.failedUserIds()).containsExactly(localFailUser.id());
    }

    private User user(String username, LocalDate startDate, LocalDate endDate) {
        return new User(
                UserId.generate(),
                Email.of(username + "@example.com"),
                FullName.of("Test", "User"),
                ZepUsername.of(username),
                null,
                new EmploymentPeriods(new EmploymentPeriod(startDate, endDate)),
                Set.of(Role.EMPLOYEE)
        );
    }
}
