package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDateCommand;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDatesResult;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDatesUseCase;
import com.gepardec.mega.hexagon.user.application.port.outbound.ZepEmployeePort;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
// No transaction: ZEP calls fan out via Mutiny, and a propagated JTA context races with commit.
@Transactional(Transactional.TxType.NEVER)
public class UpdateReleaseDatesService implements UpdateReleaseDatesUseCase {

    private final UserRepository userRepository;
    private final ZepEmployeePort zepEmployeePort;
    private final ReleaseDatePersister releaseDatePersister;

    @Inject
    public UpdateReleaseDatesService(
            UserRepository userRepository,
            ZepEmployeePort zepEmployeePort,
            ReleaseDatePersister releaseDatePersister
    ) {
        this.userRepository = userRepository;
        this.zepEmployeePort = zepEmployeePort;
        this.releaseDatePersister = releaseDatePersister;
    }

    @Override
    public UpdateReleaseDatesResult update(List<UpdateReleaseDateCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return new UpdateReleaseDatesResult(List.of());
        }

        LinkedHashSet<UserId> failedUserIds = new LinkedHashSet<>();

        // Resolve users on the calling thread: the fan-out below may run its mapper on I/O threads.
        Set<UserId> commandUserIds = commands.stream()
                .map(command -> Objects.requireNonNull(command, "command must not be null").userId())
                .collect(Collectors.toSet());
        Map<UserId, User> usersById = userRepository.findByIds(commandUserIds).stream()
                .collect(Collectors.toMap(User::id, Function.identity(), (first, second) -> first));

        List<UpdateReleaseDateCommand> commandsToSend = new ArrayList<>();
        for (UpdateReleaseDateCommand command : commands) {
            User user = usersById.get(command.userId());
            if (user == null) {
                Log.warnf("Skipping release-date update for unknown userId=%s", command.userId().value());
                failedUserIds.add(command.userId());
            } else if (user.zepUsername() == null || user.zepUsername().value().isBlank()) {
                Log.errorf("Skipping release-date update because zepUsername is missing for userId=%s", user.id().value());
                failedUserIds.add(command.userId());
            } else {
                commandsToSend.add(command);
            }
        }

        List<ZepUpdateOutcome> outcomes = Multi.createFrom().iterable(commandsToSend)
                .onItem().transformToUniAndMerge(command -> updateInZep(command, usersById.get(command.userId())))
                .collect().asList()
                .await().indefinitely();

        List<User> usersToPersist = new ArrayList<>();

        for (ZepUpdateOutcome outcome : outcomes) {
            if (!outcome.success()) {
                failedUserIds.add(outcome.command().userId());
                continue;
            }

            usersToPersist.add(outcome.user().withReleaseDate(outcome.command().releaseDate()));
        }

        for (User user : usersToPersist) {
            persistReleaseDate(user, failedUserIds);
        }

        return new UpdateReleaseDatesResult(List.copyOf(failedUserIds));
    }

    private Uni<ZepUpdateOutcome> updateInZep(UpdateReleaseDateCommand command, User user) {
        ZepUsername zepUsername = user.zepUsername();

        LocalDate releaseDate = command.releaseDate();
        return zepEmployeePort.updateReleaseDate(zepUsername, releaseDate)
                .replaceWith(ZepUpdateOutcome.success(command, user))
                .onFailure().recoverWithItem(throwable -> {
                    Log.errorf(
                            throwable,
                            "Failed to update release date in ZEP for userId=%s zepUsername=%s releaseDate=%s",
                            user.id().value(),
                            zepUsername.value(),
                            releaseDate
                    );
                    return ZepUpdateOutcome.failed(command);
                });
    }

    private void persistReleaseDate(User user, LinkedHashSet<UserId> failedUserIds) {
        try {
            releaseDatePersister.save(user);
        } catch (RuntimeException exception) {
            Log.errorf(
                    exception,
                    "Failed to persist release date locally for userId=%s releaseDate=%s",
                    user.id().value(),
                    user.releaseDate()
            );
            failedUserIds.add(user.id());
        }
    }

    private record ZepUpdateOutcome(
            UpdateReleaseDateCommand command,
            User user,
            boolean success
    ) {

        static ZepUpdateOutcome success(UpdateReleaseDateCommand command, User user) {
            return new ZepUpdateOutcome(command, user, true);
        }

        static ZepUpdateOutcome failed(UpdateReleaseDateCommand command) {
            return new ZepUpdateOutcome(command, null, false);
        }
    }
}
