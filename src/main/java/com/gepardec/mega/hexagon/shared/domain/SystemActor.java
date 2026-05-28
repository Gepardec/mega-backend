package com.gepardec.mega.hexagon.shared.domain;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.util.UUID;

public final class SystemActor {

    public static final UserId USER_ID = UserId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    private SystemActor() {
    }
}
