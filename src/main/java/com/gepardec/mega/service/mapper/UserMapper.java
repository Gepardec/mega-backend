package com.gepardec.mega.service.mapper;

import com.gepardec.mega.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserMapper implements DomainMapper<User, com.gepardec.mega.db.entity.employee.User> {
    @Override
    public com.gepardec.mega.db.entity.employee.User mapToEntity(User object) {
        com.gepardec.mega.db.entity.employee.User user = new com.gepardec.mega.db.entity.employee.User();
        user.setId(object.getDbId());
        user.setZepId(object.getUserId());
        user.setFirstname(object.getFirstname());
        user.setLastname(object.getLastname());
        user.setEmail(object.getEmail());
        user.setReleaseDate(object.getReleaseDate());
        user.setRoles(object.getRoles());
        return user;
    }

    @Override
    public User mapToDomain(com.gepardec.mega.db.entity.employee.User object) {
        if (object == null) {
            return null;
        }
        return User.builder()
                .dbId(object.getId())
                .userId(object.getZepId())
                .firstname(object.getFirstname())
                .lastname(object.getLastname())
                .email(object.getEmail())
                .releaseDate(object.getReleaseDate())
                .roles(object.getRoles())
                .build();
    }
}
