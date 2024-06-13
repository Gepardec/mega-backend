package com.gepardec.mega.service.impl.user;

import com.gepardec.mega.application.exception.ForbiddenException;
import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.domain.mapper.UserMapper;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.service.api.UserService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
public class UserServiceImplTest {

    @Inject
    UserService userService;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    UserMapper userMapper;

    @Test
    void findUserForEmail_whenNoUser_thenThrowException() {
        when(userRepository.findActiveByEmail(anyString()))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() -> userService.findUserForEmail(anyString()))
                .isInstanceOf(ForbiddenException.class);

    }

    @Test
    void findUserForEmail_whenUser_thenReturnUser() {
        com.gepardec.mega.db.entity.employee.User user = User.of("001.max.mustermann@gmail.com");

        when(userRepository.findActiveByEmail(anyString()))
                .thenReturn(Optional.of(user));

        when(userMapper.mapToDomain(any(User.class)))
                .thenReturn(com.gepardec.mega.domain.model.User.builder().userId("001-mmustermann").build());

        com.gepardec.mega.domain.model.User actual = userService.findUserForEmail(anyString());

        assertThat(actual.getUserId()).isEqualTo("001-mmustermann");
    }

    @Test
    void findByName_whenNoUser_thenThrowException() {
        when(userRepository.findActiveByName(anyString(), anyString()))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() -> userService.findByName(anyString(), anyString()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void findByName_whenUser_thenReturnUser() {
        com.gepardec.mega.db.entity.employee.User user = User.of("001.max.mustermann@gmail.com");

        when(userRepository.findActiveByName(anyString(), anyString()))
                .thenReturn(Optional.of(user));

        when(userMapper.mapToDomain(any(User.class)))
                .thenReturn(com.gepardec.mega.domain.model.User.builder().userId("001-mmustermann").build());

        com.gepardec.mega.domain.model.User actual = userService.findByName(anyString(), anyString());

        assertThat(actual.getUserId()).isEqualTo("001-mmustermann");
    }

    @Test
    void findByZepId_whenNoUser_thenThrowException() {
        when(userRepository.findByZepId(anyString()))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() -> userService.findByZepId(anyString()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void findByZepId_whenUser_thenReturnUser() {
        com.gepardec.mega.db.entity.employee.User user = User.of("001.max.mustermann@gmail.com");

        when(userRepository.findByZepId(anyString()))
                .thenReturn(Optional.of(user));

        when(userMapper.mapToDomain(any(User.class)))
                .thenReturn(com.gepardec.mega.domain.model.User.builder().userId("001-mmustermann").build());

        com.gepardec.mega.domain.model.User actual = userService.findByZepId(anyString());

        assertThat(actual.getUserId()).isEqualTo("001-mmustermann");
    }

    @Test
    void findActiveUsers_whenUser_thenReturnUser() {
        User user = User.of("001.max.mustermann@gmail.com");
        List<com.gepardec.mega.db.entity.employee.User> userList = List.of(
                user
        );

        com.gepardec.mega.domain.model.User resultUser = com.gepardec.mega.domain.model.User.builder().userId("001-mmustermann").build();
        List<com.gepardec.mega.domain.model.User> resultList = List.of(
                resultUser
        );

        when(userRepository.findActive())
                .thenReturn(userList);

        when(userMapper.mapToDomain(any(User.class)))
                .thenReturn(resultUser);

        List<com.gepardec.mega.domain.model.User> actual = userService.findActiveUsers();
        assertThat(actual).isEqualTo(resultList);
    }


    @Test
    void findByRoles_whenRolesNull_thenThrowException() {
        assertThatThrownBy(() -> userService.findByRoles(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findByRoles_whenRolesAreEmpty_thenThrowException() {
        assertThatThrownBy(() -> userService.findByRoles(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findByRoles_whenRoles_thenReturnUserList() {
        User user = User.of("001.max.mustermann@gmail.com");
        List<com.gepardec.mega.db.entity.employee.User> userList = List.of(
                user
        );

        com.gepardec.mega.domain.model.User resultUser = com.gepardec.mega.domain.model.User.builder().userId("001-mmustermann").build();
        List<com.gepardec.mega.domain.model.User> resultList = List.of(
                resultUser
        );

        when(userRepository.findByRoles(List.of(Role.EMPLOYEE)))
                .thenReturn(userList);

        when(userMapper.mapToDomain(any(User.class)))
                .thenReturn(resultUser);

        List<com.gepardec.mega.domain.model.User> actual = userService.findByRoles(List.of(Role.EMPLOYEE));
        assertThat(actual).isEqualTo(resultList);
    }

}
