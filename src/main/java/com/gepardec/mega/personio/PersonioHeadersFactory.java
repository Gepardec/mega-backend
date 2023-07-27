package com.gepardec.mega.personio;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import java.util.UUID;
@ApplicationScoped
public class PersonioHeadersFactory implements ClientHeadersFactory {
    private static final String APP_ID = "GEPARDEC_MEGA";
    private static final String PAPI = "Bearer <token>";

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
        result.add("X-Personio-App-ID", APP_ID);
        result.add("authorization", PAPI);
        return result;
    }
}
