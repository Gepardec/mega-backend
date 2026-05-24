package com.gepardec.mega.hexagon.user.domain.error;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class UnknownUsersException extends RuntimeException {

    private final transient Set<ZepUsername> unknownUsers;

    public UnknownUsersException(Set<ZepUsername> unknownUsers) {
        super("Unknown users: " + toMessage(Objects.requireNonNull(unknownUsers, "unknownUsers must not be null")));
        this.unknownUsers = Set.copyOf(unknownUsers);
    }

    public Set<ZepUsername> unknownUsers() {
        if (unknownUsers == null) {
            return Set.of();
        }
        return unknownUsers;
    }

    private static String toMessage(Set<ZepUsername> unknownUsers) {
        if (unknownUsers == null || unknownUsers.isEmpty()) {
            return "";
        }
        return unknownUsers.stream()
                .map(ZepUsername::value)
                .sorted()
                .collect(Collectors.joining(", "));
    }
}
