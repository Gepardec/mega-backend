package com.gepardec.mega.application.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Rejects unknown properties in requests to our own API, as the contract declares
 * {@code additionalProperties: false}. REST clients keep the Quarkus default and ignore unknown
 * properties, because external systems (ZEP, Personio) add response fields whenever they like.
 */
@Provider
@ConstrainedTo(RuntimeType.SERVER)
public class StrictServerObjectMapperResolver implements ContextResolver<ObjectMapper> {

    private final ObjectMapper strictObjectMapper;

    @Inject
    public StrictServerObjectMapperResolver(ObjectMapper objectMapper) {
        this.strictObjectMapper = objectMapper.copy()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return strictObjectMapper;
    }
}
