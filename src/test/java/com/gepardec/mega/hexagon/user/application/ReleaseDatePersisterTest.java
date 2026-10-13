package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReleaseDatePersisterTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ReleaseDatePersister persister;

    @Test
    void save_shouldDelegateSingleUserToRepository() {
        User user = Instancio.create(User.class);

        persister.save(user);

        verify(userRepository).saveAll(List.of(user));
    }
}
