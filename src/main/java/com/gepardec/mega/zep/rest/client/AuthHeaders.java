package com.gepardec.mega.zep.rest.client;

import com.gepardec.mega.application.configuration.ZepConfig;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

public class AuthHeaders implements ClientHeadersFactory {

    @Inject
    ZepConfig zepConfig;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + zepConfig.getRestBearerToken());
        return headers;
    }
}
