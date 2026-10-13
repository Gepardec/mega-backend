package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDateCommand;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDatesResult;
import com.gepardec.mega.hexagon.user.application.port.outbound.ZepEmployeePort;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import io.smallrye.mutiny.Uni;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateReleaseDatesServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    ZepEmployeePort zepEmployeePort;

    @Mock
    ReleaseDatePersister releaseDatePersister;

    UpdateReleaseDatesService service;

    @BeforeEach
    void setUp() {
        service = new UpdateReleaseDatesService(userRepository, zepEmployeePort, releaseDatePersister);
    }

    @Test
    void update_shouldFanOutConcurrentlyAndPersistSuccessfulUpdates() {
        User firstUser = user("first");
        User secondUser = user("second");
        LocalDate releaseDate = LocalDate.of(2026, 4, 30);

        UpdateReleaseDateCommand firstCommand = new UpdateReleaseDateCommand(firstUser.id(), releaseDate);
        UpdateReleaseDateCommand secondCommand = new UpdateReleaseDateCommand(secondUser.id(), releaseDate);

        when(userRepository.findByIds(Set.of(firstUser.id(), secondUser.id()))).thenReturn(List.of(firstUser, secondUser));
        when(zepEmployeePort.updateReleaseDate(firstUser.zepUsername(), releaseDate))
                .thenReturn(Uni.createFrom().voidItem().onItem().delayIt().by(Duration.ofMillis(300)));
        when(zepEmployeePort.updateReleaseDate(secondUser.zepUsername(), releaseDate))
                .thenReturn(Uni.createFrom().voidItem().onItem().delayIt().by(Duration.ofMillis(300)));

        long startNanos = System.nanoTime();
        UpdateReleaseDatesResult result = service.update(List.of(firstCommand, secondCommand));
        long elapsedMillis = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();

        assertThat(result.failedUserIds()).isEmpty();
        assertThat(elapsedMillis).isLessThan(550);

        verify(userRepository).findByIds(Set.of(firstUser.id(), secondUser.id()));
        verify(userRepository, never()).findById(any());
        verify(releaseDatePersister).save(firstUser.withReleaseDate(releaseDate));
        verify(releaseDatePersister).save(secondUser.withReleaseDate(releaseDate));
    }

    @Test
    void update_shouldCollectFailedIdsForUnknownUsersZepFailuresAndLocalPersistenceFailures() {
        User successfulUser = user("success");
        User zepFailUser = user("zep-fail");
        User localFailUser = user("local-fail");
        UserId unknownUserId = UserId.of(Instancio.create(UUID.class));
        LocalDate releaseDate = LocalDate.of(2026, 4, 30);

        UpdateReleaseDateCommand successCommand = new UpdateReleaseDateCommand(successfulUser.id(), releaseDate);
        UpdateReleaseDateCommand zepFailCommand = new UpdateReleaseDateCommand(zepFailUser.id(), releaseDate);
        UpdateReleaseDateCommand localFailCommand = new UpdateReleaseDateCommand(localFailUser.id(), releaseDate);
        UpdateReleaseDateCommand unknownCommand = new UpdateReleaseDateCommand(unknownUserId, releaseDate);

        when(userRepository.findByIds(Set.of(successfulUser.id(), zepFailUser.id(), localFailUser.id(), unknownUserId)))
                .thenReturn(List.of(successfulUser, zepFailUser, localFailUser));

        when(zepEmployeePort.updateReleaseDate(successfulUser.zepUsername(), releaseDate)).thenReturn(Uni.createFrom().voidItem());
        when(zepEmployeePort.updateReleaseDate(zepFailUser.zepUsername(), releaseDate))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("zep unavailable")));
        when(zepEmployeePort.updateReleaseDate(localFailUser.zepUsername(), releaseDate)).thenReturn(Uni.createFrom().voidItem());

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.id().equals(localFailUser.id())) {
                throw new RuntimeException("db write failed");
            }
            return null;
        }).when(releaseDatePersister).save(any());

        UpdateReleaseDatesResult result = service.update(List.of(successCommand, zepFailCommand, localFailCommand, unknownCommand));

        assertThat(result.failedUserIds()).containsExactlyInAnyOrder(unknownUserId, zepFailUser.id(), localFailUser.id());
        verify(zepEmployeePort, never()).updateReleaseDate(ZepUsername.of("unknown"), releaseDate);
        verify(releaseDatePersister).save(successfulUser.withReleaseDate(releaseDate));
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
