package com.gepardec.mega.personio.commons.factory;

import com.gepardec.mega.application.configuration.PersonioConfig;
import com.gepardec.mega.personio.auth.AuthResponse;
import com.gepardec.mega.personio.auth.AuthResponseData;
import com.gepardec.mega.personio.auth.PersonioAuthClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.apache.http.HttpHeaders;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class PersonioHeadersFactoryTest {

    @Inject
    PersonioHeadersFactory personioHeadersFactory;

    @InjectMock
    @RestClient
    PersonioAuthClient personioAuthClient;

    @InjectMock
    PersonioConfig personioConfig;

    @InjectSpy
    PersonioApiToken personioApiToken;

    @Test
    void update_SessionValid_AuthenticateNotCalled() {
        //GIVEN
        when(personioApiToken.getToken()).thenReturn("valid-token");
        when(personioApiToken.getExpiresAt()).thenReturn(LocalDateTime.now().plusMinutes(15));

        //WHEN
        var result = personioHeadersFactory.update(new MultivaluedHashMap<>(), new MultivaluedHashMap<>());

        //THEN
        verify(personioAuthClient, times(0)).authenticate(any());

        assertThat(result).hasSize(2);
        assertThat(result).containsKey(HttpHeaders.AUTHORIZATION);
        assertThat(result.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer valid-token");
    }

    @Test
    void update_SessionInvalid_AuthenticateCalled() {
        //GIVEN
        var authResponseData = new AuthResponseData();
        authResponseData.setToken("generated-token");

        var authResponse = new AuthResponse();
        authResponse.setSuccess(true);
        authResponse.setData(authResponseData);

        when(personioAuthClient.authenticate(any())).thenReturn(authResponse);
        when(personioConfig.getExpiresInMinutes()).thenReturn(30L);

        //WHEN
        var result = personioHeadersFactory.update(new MultivaluedHashMap<>(), new MultivaluedHashMap<>());

        //THEN
        verify(personioAuthClient).authenticate(any());

        assertThat(result).hasSize(2);
        assertThat(result).containsKey(HttpHeaders.AUTHORIZATION);
        assertThat(result.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer generated-token");
    }

    @Test
    void update_AuthenticateFailed_NotAuthorizedException() {
        //GIVEN
        var authResponse = new AuthResponse();
        authResponse.setSuccess(false);

        when(personioAuthClient.authenticate(any())).thenReturn(authResponse);

        //WHEN
        //THEN
        assertThatExceptionOfType(NotAuthorizedException.class)
                .isThrownBy(() -> personioHeadersFactory.update(new MultivaluedHashMap<>(), new MultivaluedHashMap<>()))
                .withMessage("HTTP 401 Unauthorized");
    }
}
