package com.gepardec.mega.service.impl;

import com.gepardec.mega.application.exception.ForbiddenException;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.domain.mapper.UserMapper;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.service.api.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class UserServiceImpl implements UserService {

    @Inject
    UserMapper mapper;

    @Inject
    UserRepository userRepository;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public User findUserForEmail(final String email) {
        final com.gepardec.mega.db.entity.employee.User user = userRepository.findActiveByEmail(email)
                .orElseThrow(() -> new ForbiddenException("User with email '" + email + "' is either unknown or inactive"));

        return mapper.mapToDomain(user);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public User findByName(String firstname, String lastname) {
        var user = userRepository.findActiveByName(firstname, lastname)
                .orElseThrow(() ->
                        new ForbiddenException("User with name '" + firstname + " " + lastname +
                                "' is either unknown or inactive")
                );

        return mapper.mapToDomain(user);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public User findByZepId(String zepId) {
        var user = userRepository.findByZepId(zepId)
                .orElseThrow(() ->
                        new ForbiddenException("User with zepId '" + zepId + "' is either unknown or inactive")
                );

        return mapper.mapToDomain(user);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<User> findActiveUsers() {
        final List<com.gepardec.mega.db.entity.employee.User> activeUsers = userRepository.findActive();
        return activeUsers.stream()
                .map(mapper::mapToDomain)
                .toList();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<User> findByRoles(List<Role> roles) {
        if (roles == null || roles.size() == 0) {
            throw new IllegalArgumentException("Cannot load users if no 'roles' are given");
        }

        return userRepository.findByRoles(roles).stream()
                .map(mapper::mapToDomain)
                .toList();
    }
}
