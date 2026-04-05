package com.gepardec.mega.hexagon.user.domain.port.outbound;

import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository {

    Optional<User> findByEmail(Email email);

    Optional<User> findByZepUsername(String username);

    List<User> findAll();

    List<User> findByIds(Set<UserId> userIds);

    void saveAll(List<User> users);
}
