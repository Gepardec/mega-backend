package com.gepardec.mega.hexagon.user.domain.port.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository {

    Optional<User> findById(UserId userId);

    Optional<User> findByEmail(Email email);

    Optional<User> findByFullName(FullName fullName);

    Optional<User> findByZepUsername(ZepUsername username);

    List<User> findByRole(Role role);

    List<User> findByZepUsernames(Set<ZepUsername> usernames);

    List<User> findAll();

    List<User> findByIds(Set<UserId> userIds);

    void saveAll(List<User> users);
}
