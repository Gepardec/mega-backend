package com.gepardec.mega.service.api;

import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;

import java.util.List;
import java.util.Set;

public interface UserService {

    User findUserForEmail(String email);

    User findByName(String firstname, String lastname);

    User findByZepId(String zepId);

    List<User> findByZepIds(Set<String> zepIds);

    List<User> findActiveUsers();

    List<User> findByRoles(List<Role> roles);
}
