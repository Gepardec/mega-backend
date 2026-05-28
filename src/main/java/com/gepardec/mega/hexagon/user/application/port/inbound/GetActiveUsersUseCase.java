package com.gepardec.mega.hexagon.user.application.port.inbound;

import com.gepardec.mega.hexagon.user.domain.model.User;

import java.util.List;

public interface GetActiveUsersUseCase {

    List<User> getActiveUsers();
}
