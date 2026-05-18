package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDateCommand;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDatesResult;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDatesUseCase;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.user.domain.port.outbound.ZepEmployeePort;
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
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class UpdateReleaseDatesService implements UpdateReleaseDatesUseCase {

    private final UserRepository userRepository;
    private final ZepEmployeePort zepEmployeePort;

    @Inject
    public UpdateReleaseDatesService(UserRepository userRepository, ZepEmployeePort zepEmployeePort) {
        this.userRepository = userRepository;
        this.zepEmployeePort = zepEmployeePort;
    }

    @Override
    public UpdateReleaseDatesResult update(List<UpdateReleaseDateCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return new UpdateReleaseDatesResult(List.of());
        }

        List<ZepUpdateOutcome> outcomes = Multi.createFrom().iterable(commands)
                .onItem().transformToUniAndMerge(this::updateInZep)
                .collect().asList()
                .await().indefinitely();

        LinkedHashSet<UserId> failedUserIds = new LinkedHashSet<>();
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

    private Uni<ZepUpdateOutcome> updateInZep(UpdateReleaseDateCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        Optional<User> maybeUser = userRepository.findById(command.userId());
        if (maybeUser.isEmpty()) {
            Log.warnf("Skipping release-date update for unknown userId=%s", command.userId().value());
            return Uni.createFrom().item(ZepUpdateOutcome.failed(command));
        }

        User user = maybeUser.get();
        ZepUsername zepUsername = user.zepUsername();
        if (zepUsername == null || zepUsername.value().isBlank()) {
            Log.errorf("Skipping release-date update because zepUsername is missing for userId=%s", user.id().value());
            return Uni.createFrom().item(ZepUpdateOutcome.failed(command));
        }

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
            userRepository.saveAll(List.of(user));
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
