package com.gepardec.mega.service.impl.user;

import com.gepardec.mega.application.exception.ForbiddenException;
import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.domain.mapper.UserMapper;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.service.api.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@QuarkusTest
public class UserServiceImplTest {

    @InjectMock
    UserRepository repository;

    @InjectMock
    UserMapper userMapper;

    @Inject
    UserService service;

    @Test
    void findUserForEmail_returnsUser() {
        when(repository.findActiveByEmail(anyString()))
                .thenReturn(Optional.of(createUser()));

        when(userMapper.mapToDomain(any(User.class)))
                .thenReturn(createDomainUser());

        com.gepardec.mega.domain.model.User actual = service.findUserForEmail("testemail@gmail.com");

        assertThat(actual.getUserId()).isEqualTo("1L");
        assertThat(actual.getEmail()).isEqualTo("testemail@gmail.com");
    }

    @Test
    void findUserByName_whenUserWithName_returnsUser() {
        when(repository.findActiveByName(anyString(), anyString()))
                .thenReturn(Optional.of(createUser()));

        when(userMapper.mapToDomain(any(User.class)))
                .thenReturn(createDomainUser());

        com.gepardec.mega.domain.model.User actual = service.findByName("Test", "User");

        assertThat(actual.getUserId()).isEqualTo("1L");
        assertThat(actual.getEmail()).isEqualTo("testemail@gmail.com");

    }

    @Test
    void findUserByName_whenNoUserWithName_throwsException() {
        when(repository.findActiveByName(anyString(), anyString()))
                .thenThrow(new ForbiddenException());

        assertThatThrownBy(() -> repository.findActiveByName("Test", "User"))
                .isInstanceOf(ForbiddenException.class);

        verify(repository, times(1)).findActiveByName(anyString(), anyString());
        verifyNoMoreInteractions(repository);

    }

    @Test
    void findUserByZepId_whenUserWithId_returnsUser() {
        when(repository.findByZepId(anyString()))
                .thenReturn(Optional.of(createUser()));

        when(userMapper.mapToDomain(any(User.class)))
                .thenReturn(createDomainUser());

        com.gepardec.mega.domain.model.User actual = service.findByZepId("007");

        assertThat(actual.getUserId()).isEqualTo("1L");
        assertThat(actual.getEmail()).isEqualTo("testemail@gmail.com");

    }

    @Test
    void findUsersByRoles_whenNoUsersWithRoles_throwsException() {
        List<Role> emptyRoles = new ArrayList<>();

        assertThatThrownBy(() -> service.findByRoles(emptyRoles))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot load users if no 'roles' are given");


        verify(repository, never()).findByRoles(anyList());

    }

    @Test
    void findUsersByRoles_whenUsersWithRolesPresent_returnsUsers() {
        List<Role> roles = new ArrayList<>();
        roles.add(Role.EMPLOYEE);
        roles.add(Role.PROJECT_LEAD);

        when(repository.findByRoles(roles))
                .thenReturn(createUserList());

        when(userMapper.mapToDomain(any(User.class)))
                .thenReturn(createDomainUser());

        List<com.gepardec.mega.domain.model.User> actual = service.findByRoles(roles);

        assertThat(actual.size()).isEqualTo(1);

    }

    private com.gepardec.mega.db.entity.employee.User createUser() {
        com.gepardec.mega.db.entity.employee.User user = new com.gepardec.mega.db.entity.employee.User();
        user.setId(1L);
        user.setZepId("007");
        user.setEmail("testemail@email.com");
        user.setFirstname("Test");
        user.setFirstname("User");
        return user;
    }

    private com.gepardec.mega.domain.model.User createDomainUser() {
        return com.gepardec.mega.domain.model.User.builder()
                .userId("1L")
                .email("testemail@gmail.com")
                .firstname("Test")
                .lastname("User")
                .build();
    }

    private List<com.gepardec.mega.db.entity.employee.User> createUserList() {
        List<com.gepardec.mega.db.entity.employee.User> users = new ArrayList<>();
        users.add(createUser());
        return users;
    }
}
