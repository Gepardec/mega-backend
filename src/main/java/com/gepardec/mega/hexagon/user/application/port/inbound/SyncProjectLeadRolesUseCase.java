package com.gepardec.mega.hexagon.user.application.port.inbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.util.Set;

public interface SyncProjectLeadRolesUseCase {

    SyncProjectLeadRolesResult sync(Set<UserId> leadUserIds);
}
