package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.rest.model.UserDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserMapper implements DtoMapper<User, UserDto> {

    @Override
    public UserDto mapToDto(User object) {
        return UserDto.builder()
                .dbId(object.getDbId())
                .userId(object.getUserId())
                .email(object.getEmail())
                .firstname(object.getFirstname())
                .lastname(object.getLastname())
                .releaseDate(object.getReleaseDate())
                .roles(object.getRoles())
                .personioId(object.getPersonioId())
                .build();
    }

    @Override
    public User mapToDomain(UserDto object) {
        return User.builder()
                .dbId(object.getDbId())
                .userId(object.getUserId())
                .email(object.getEmail())
                .firstname(object.getFirstname())
                .lastname(object.getLastname())
                .releaseDate(object.getReleaseDate())
                .roles(object.getRoles())
                .personioId(object.getPersonioId())
                .build();
    }
}
