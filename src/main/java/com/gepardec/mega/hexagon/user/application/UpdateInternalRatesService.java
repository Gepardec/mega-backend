package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.application.port.inbound.InternalRateUpdateCommand;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateInternalRatesUseCase;
import com.gepardec.mega.hexagon.user.application.port.outbound.ZepEmployeePort;
import com.gepardec.mega.hexagon.user.domain.error.UnknownUsersException;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class UpdateInternalRatesService implements UpdateInternalRatesUseCase {

    private final UserRepository userRepository;
    private final ZepEmployeePort zepEmployeePort;

    @Inject
    public UpdateInternalRatesService(UserRepository userRepository, ZepEmployeePort zepEmployeePort) {
        this.userRepository = userRepository;
        this.zepEmployeePort = zepEmployeePort;
    }

    @Override
    public void update(List<InternalRateUpdateCommand> commands) {
        List<InternalRateUpdateCommand> sanitizedCommands = sanitize(commands);
        if (sanitizedCommands.isEmpty()) {
            return;
        }

        Set<ZepUsername> requestedUsernames = sanitizedCommands.stream()
                .map(InternalRateUpdateCommand::zepUsername)
                .collect(Collectors.toSet());

        Set<ZepUsername> knownUsernames = userRepository.findByZepUsernames(requestedUsernames).stream()
                .map(User::zepUsername)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<ZepUsername> unknownUsers = requestedUsernames.stream()
                .filter(username -> !knownUsernames.contains(username))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (!unknownUsers.isEmpty()) {
            throw new UnknownUsersException(unknownUsers);
        }

        Multi.createFrom().iterable(sanitizedCommands)
                .onItem().transformToUniAndMerge(command -> zepEmployeePort.updateHourlyRate(
                        command.zepUsername(),
                        command.hourlyRate(),
                        command.effectiveFrom()
                ))
                .collect().asList()
                .await().indefinitely();
    }

    private List<InternalRateUpdateCommand> sanitize(List<InternalRateUpdateCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return List.of();
        }
        return commands.stream()
                .filter(Objects::nonNull)
                .toList();
    }
}
