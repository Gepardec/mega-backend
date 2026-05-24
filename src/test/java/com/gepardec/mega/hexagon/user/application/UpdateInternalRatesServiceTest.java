package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.application.port.inbound.InternalRateUpdateCommand;
import com.gepardec.mega.hexagon.user.domain.error.UnknownUsersException;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.HourlyRate;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.user.domain.port.outbound.ZepEmployeePort;
import io.smallrye.mutiny.Uni;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateInternalRatesServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    ZepEmployeePort zepEmployeePort;

    UpdateInternalRatesService service;

    @BeforeEach
    void setUp() {
        service = new UpdateInternalRatesService(userRepository, zepEmployeePort);
    }

    @Test
    void update_shouldCallZepForEachCommand_whenAllUsersExist() {
        InternalRateUpdateCommand first = command("first", 72.0d, LocalDate.of(2026, 1, 1));
        InternalRateUpdateCommand second = command("second", 84.5d, LocalDate.of(2026, 2, 1));

        when(userRepository.findByZepUsernames(Set.of(first.zepUsername(), second.zepUsername())))
                .thenReturn(List.of(user("first"), user("second")));
        when(zepEmployeePort.updateHourlyRate(first.zepUsername(), first.hourlyRate(), first.effectiveFrom()))
                .thenReturn(Uni.createFrom().voidItem());
        when(zepEmployeePort.updateHourlyRate(second.zepUsername(), second.hourlyRate(), second.effectiveFrom()))
                .thenReturn(Uni.createFrom().voidItem());

        service.update(List.of(first, second));

        verify(zepEmployeePort).updateHourlyRate(first.zepUsername(), first.hourlyRate(), first.effectiveFrom());
        verify(zepEmployeePort).updateHourlyRate(second.zepUsername(), second.hourlyRate(), second.effectiveFrom());
    }

    @Test
    void update_shouldThrowUnknownUsersExceptionAndSkipZep_whenUnknownUsersArePresent() {
        InternalRateUpdateCommand known = command("known", 72.0d, LocalDate.of(2026, 1, 1));
        InternalRateUpdateCommand unknownFirst = command("missing-a", 80.0d, LocalDate.of(2026, 1, 1));
        InternalRateUpdateCommand unknownSecond = command("missing-b", 81.0d, LocalDate.of(2026, 1, 2));

        when(userRepository.findByZepUsernames(Set.of(
                known.zepUsername(),
                unknownFirst.zepUsername(),
                unknownSecond.zepUsername()
        ))).thenReturn(List.of(user("known")));

        ThrowableAssert.ThrowingCallable throwingCallable = () -> service.update(List.of(known, unknownFirst, unknownSecond));

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(UnknownUsersException.class)
                .satisfies(throwable -> assertThat(((UnknownUsersException) throwable).unknownUsers())
                        .containsExactlyInAnyOrder(unknownFirst.zepUsername(), unknownSecond.zepUsername()));

        verifyNoInteractions(zepEmployeePort);
    }

    private InternalRateUpdateCommand command(String username, double rate, LocalDate effectiveFrom) {
        return new InternalRateUpdateCommand(
                ZepUsername.of(username),
                HourlyRate.of(rate),
                effectiveFrom
        );
    }

    private User user(String username) {
        return new User(
                UserId.generate(),
                Email.of(username + "@example.com"),
                FullName.of("Test", "User"),
                ZepUsername.of(username),
                null,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2024, 1, 1), null)),
                Set.of(Role.EMPLOYEE)
        );
    }
}
