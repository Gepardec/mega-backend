package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.shared.application.security.ForbiddenException;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterDuplicateEmailTest {

    @Mock
    private UserPanacheRepository panache;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    @Test
    void findByEmail_shouldThrowWhenMultipleUsersShareSameEmail() {
        // Given
        String email = "employee@example.com";
        when(panache.list("email", email)).thenReturn(List.of(new UserEntity(), new UserEntity()));

        // When
        ThrowableAssert.ThrowingCallable throwingCallable = () -> adapter.findByEmail(Email.of(email));

        // Then
        assertThatThrownBy(throwingCallable)
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("authenticated actor resolution is ambiguous")
                .hasMessageContaining(email);
    }
}
