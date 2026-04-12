package com.gepardec.mega.hexagon.project.domain.port.outbound;

import com.gepardec.mega.hexagon.user.domain.model.ZepUsername;

import java.util.Optional;
import java.util.UUID;

public interface UserLookupPort {

    Optional<UUID> findUserIdByZepUsername(ZepUsername username);
}
