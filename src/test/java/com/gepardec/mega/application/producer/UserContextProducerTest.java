package com.gepardec.mega.application.producer;

import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.service.api.UserService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@QuarkusTest
class UserContextProducerTest {

    @InjectMock
    UserService userService;

    @Inject
    UserContextProducer producer;

    @Test
    @TestSecurity(user = "test")
    @JwtSecurity(claims = {
            @Claim(key = "email", value = "test@gepardec.com")
    })
    void createUserContext_whenUserVerified_thenUserSetAndLogged() {
        // Given
        final User user = User.builder()
                .dbId(1)
                .userId("1")
                .firstname("Max")
                .lastname("Mustermann")
                .email("no-reply@gepardec.com")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        when(userService.findUserForEmail("test@gepardec.com")).thenReturn(user);

        // When
        final UserContext userContext = producer.createUserContext();

        // Then
        assertThat(userContext.getUser()).isNotNull();
        assertThat(userContext.getUser()).isEqualTo(user);
    }
}
