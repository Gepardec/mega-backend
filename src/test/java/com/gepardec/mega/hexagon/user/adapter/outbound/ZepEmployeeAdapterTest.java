package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepEmployeeReleaseDateUpdate;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZepEmployeeAdapterTest {

    @Mock
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Mock
    ZepService zepService;

    ZepEmployeeAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ZepEmployeeAdapter();
        adapter.zepEmployeeRestClient = zepEmployeeRestClient;
        adapter.zepService = zepService;
    }

    @Test
    void updateReleaseDate_whenCalled_thenSendsUsernameAsPathAndInBody() {
        // Given
        ZepUsername username = ZepUsername.of("007-jbond");
        LocalDate releaseDate = LocalDate.of(2026, 9, 30);
        when(zepEmployeeRestClient.updateReleaseDate(any(), any())).thenReturn(Uni.createFrom().voidItem());

        // When
        adapter.updateReleaseDate(username, releaseDate).await().indefinitely();

        // Then
        ArgumentCaptor<ZepEmployeeReleaseDateUpdate> bodyCaptor = ArgumentCaptor.forClass(ZepEmployeeReleaseDateUpdate.class);
        verify(zepEmployeeRestClient).updateReleaseDate(eq("007-jbond"), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).isEqualTo(new ZepEmployeeReleaseDateUpdate("007-jbond", releaseDate));
    }

    @Test
    void updateReleaseDate_whenCalled_thenLegacyZepServiceIsNotUsed() {
        // Given
        when(zepEmployeeRestClient.updateReleaseDate(any(), any())).thenReturn(Uni.createFrom().voidItem());

        // When
        adapter.updateReleaseDate(ZepUsername.of("007-jbond"), LocalDate.of(2026, 9, 30)).await().indefinitely();

        // Then
        verifyNoInteractions(zepService);
    }

    @Test
    void updateReleaseDate_whenClientFails_thenReturnedUniFails() {
        // Given
        WebApplicationException notFound = new WebApplicationException(404);
        when(zepEmployeeRestClient.updateReleaseDate(any(), any())).thenReturn(Uni.createFrom().failure(notFound));

        // When
        Uni<Void> result = adapter.updateReleaseDate(ZepUsername.of("007-jbond"), LocalDate.of(2026, 9, 30));

        // Then
        assertThatThrownBy(() -> result.await().indefinitely()).isSameAs(notFound);
    }
}
