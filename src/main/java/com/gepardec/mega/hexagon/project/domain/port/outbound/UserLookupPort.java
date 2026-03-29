package com.gepardec.mega.hexagon.project.domain.port.outbound;

import java.util.Optional;
import java.util.UUID;

public interface UserLookupPort {

    Optional<UUID> findUserIdByZepUsername(String username);
}
