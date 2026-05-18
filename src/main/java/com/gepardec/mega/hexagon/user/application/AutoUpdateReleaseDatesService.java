package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.application.port.inbound.AutoUpdateReleaseDatesResult;
import com.gepardec.mega.hexagon.user.application.port.inbound.AutoUpdateReleaseDatesUseCase;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.PayrollMonthCompletionPort;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.user.domain.port.outbound.ZepEmployeePort;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
@Transactional
public class AutoUpdateReleaseDatesService implements AutoUpdateReleaseDatesUseCase {

    private final UserRepository userRepository;
    private final ZepEmployeePort zepEmployeePort;
    private final PayrollMonthCompletionPort payrollMonthCompletionPort;
    private final Clock clock;

    @Inject
    public AutoUpdateReleaseDatesService(
            UserRepository userRepository,
            ZepEmployeePort zepEmployeePort,
            PayrollMonthCompletionPort payrollMonthCompletionPort,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.zepEmployeePort = zepEmployeePort;
        this.payrollMonthCompletionPort = payrollMonthCompletionPort;
        this.clock = clock;
    }

    @Override
    public AutoUpdateReleaseDatesResult autoUpdate() {
        YearMonth payrollMonth = YearMonth.now(clock).minusMonths(1);
        LocalDate releaseDate = payrollMonth.atEndOfMonth();
        Set<UserId> completedTaskUsers = payrollMonthCompletionPort.findUsersWithAllTasksCompleted(payrollMonth);

        List<User> usersToUpdate = userRepository.findAll().stream()
                .filter(user -> user.isActiveIn(payrollMonth))
                .filter(user -> completedTaskUsers.contains(user.id()))
                .toList();

        if (usersToUpdate.isEmpty()) {
            return new AutoUpdateReleaseDatesResult(payrollMonth, releaseDate, List.of(), List.of());
        }

        List<ZepUpdateOutcome> outcomes = Multi.createFrom().iterable(usersToUpdate)
                .onItem().transformToUniAndMerge(user -> updateInZep(user, releaseDate))
                .collect().asList()
                .await().indefinitely();

        LinkedHashSet<UserId> updatedUserIds = new LinkedHashSet<>();
        LinkedHashSet<UserId> failedUserIds = new LinkedHashSet<>();

        for (ZepUpdateOutcome outcome : outcomes) {
            if (!outcome.success()) {
                failedUserIds.add(outcome.user().id());
                continue;
            }

            User updatedUser = outcome.user().withReleaseDate(releaseDate);
            if (persistReleaseDate(updatedUser)) {
                updatedUserIds.add(updatedUser.id());
            } else {
                failedUserIds.add(updatedUser.id());
            }
        }

        return new AutoUpdateReleaseDatesResult(
                payrollMonth,
                releaseDate,
                List.copyOf(updatedUserIds),
                List.copyOf(failedUserIds)
        );
    }

    private Uni<ZepUpdateOutcome> updateInZep(User user, LocalDate releaseDate) {
        ZepUsername zepUsername = user.zepUsername();
        if (zepUsername == null || zepUsername.value().isBlank()) {
            Log.errorf(
                    "Skipping auto release-date update because zepUsername is missing for userId=%s",
                    user.id().value()
            );
            return Uni.createFrom().item(ZepUpdateOutcome.failed(user));
        }

        return zepEmployeePort.updateReleaseDate(zepUsername, releaseDate)
                .replaceWith(ZepUpdateOutcome.success(user))
                .onFailure().recoverWithItem(throwable -> {
                    Log.errorf(
                            throwable,
                            "Failed auto release-date update in ZEP for userId=%s zepUsername=%s releaseDate=%s",
                            user.id().value(),
                            zepUsername.value(),
                            releaseDate
                    );
                    return ZepUpdateOutcome.failed(user);
                });
    }

    private boolean persistReleaseDate(User user) {
        try {
            userRepository.saveAll(List.of(user));
            return true;
        } catch (RuntimeException exception) {
            Log.errorf(
                    exception,
                    "Failed to persist auto-updated release date locally for userId=%s releaseDate=%s",
                    user.id().value(),
                    user.releaseDate()
            );
            return false;
        }
    }

    private record ZepUpdateOutcome(User user, boolean success) {

        static ZepUpdateOutcome success(User user) {
            return new ZepUpdateOutcome(user, true);
        }

        static ZepUpdateOutcome failed(User user) {
            return new ZepUpdateOutcome(user, false);
        }
    }
}
