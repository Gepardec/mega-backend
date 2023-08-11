package com.gepardec.mega.personio.factory;

import com.gepardec.mega.application.configuration.PersonioConfig;
import com.gepardec.mega.personio.client.PersonioAuthClient;
import com.gepardec.mega.personio.model.AuthResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.http.HttpHeaders;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class PersonioHeadersFactory implements ClientHeadersFactory {

    public static final String X_PERSONIO_APP_ID = "X-Personio-App-ID";

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String APP_ID = "GEPARDEC_MEGA";

    @Inject
    @RestClient
    PersonioAuthClient personioAuthClient;

    @Inject
    PersonioConfig personioConfig;

    @Inject
    PersonioApiToken personioApiToken;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();

        result.add(X_PERSONIO_APP_ID, APP_ID);
        result.add(HttpHeaders.AUTHORIZATION, BEARER_PREFIX.concat(authenticate().getToken()));

        return result;
    }

    private PersonioApiToken authenticate() {
        if (isSessionValid(personioApiToken)) {
            return personioApiToken;
        }

        PersonioClientToken personioClientToken = new PersonioClientToken();
        personioClientToken.setClientSecret(personioConfig.getClientSecret());
        personioClientToken.setClientId(personioConfig.getClientId());

        AuthResponse authenticateResponse = personioAuthClient.authenticate(personioClientToken);
        if (authenticateResponse != null && authenticateResponse.isSuccess()) {
            personioApiToken.setToken(authenticateResponse.getData().getToken());
            personioApiToken.setExpiresAt(LocalDateTime.now().plusMinutes(personioConfig.getExpiresInMinutes()));

            return personioApiToken;
        }

        throw new NotAuthorizedException("Authentication to Personio failed.");
    }

    private boolean isSessionValid(PersonioApiToken token) {
        return Optional.ofNullable(token).isPresent() &&
                Optional.ofNullable(token.getToken()).isPresent() &&
                Optional.ofNullable(token.getExpiresAt()).isPresent() &&
                token.getExpiresAt().isAfter(LocalDateTime.now());
    }
}
