package com.gepardec.mega.db.mapper;

import com.gepardec.mega.db.entity.employee.UserEntity;
import com.gepardec.mega.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserMapper implements EntityMapper<User, UserEntity> {
    @Override
    public UserEntity mapToEntity(User object) {
        UserEntity user = new UserEntity();
        user.setId(object.getDbId());
        user.setZepId(object.getUserId());
        user.setFirstname(object.getFirstname());
        user.setLastname(object.getLastname());
        user.setEmail(object.getEmail());
        user.setReleaseDate(object.getReleaseDate());
        user.setRoles(object.getRoles());
        user.setPersonioId(object.getPersonioId());
        return user;
    }

    @Override
    public User mapToDomain(UserEntity object) {
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
                .personioId(object.getPersonioId())
                .build();
    }
}
