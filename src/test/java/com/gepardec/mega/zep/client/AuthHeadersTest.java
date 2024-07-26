package com.gepardec.mega.zep.client;


import com.gepardec.mega.application.configuration.ZepConfig;
import com.gepardec.mega.zep.rest.client.AuthHeaders;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@QuarkusTest
class AuthHeadersTest {

    @InjectMock
    ZepConfig zepConfig;

    @Inject
    AuthHeaders headers;

    @Test
    void getHeader() {
        when(zepConfig.getRestBearerToken()).thenReturn("token");

        MultivaluedMap<String, String> reference = new MultivaluedHashMap<>();
        reference.add(HttpHeaders.AUTHORIZATION, "Bearer token");

        assertThat(headers.update(null, null))
                .usingRecursiveComparison()
                .isEqualTo(reference);
    }
}
