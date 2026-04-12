package com.gepardec.mega.hexagon.project.domain.port.outbound;

import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.ZepUsername;

import java.util.Optional;

public interface UserLookupPort {

    Optional<UserId> findUserIdByZepUsername(ZepUsername username);
}
