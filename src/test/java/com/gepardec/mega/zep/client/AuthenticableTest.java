package com.gepardec.mega.zep.client;


import com.gepardec.mega.zep.rest.client.Authenticatable;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class AuthenticableTest {

    @Test
    void getHeader() {
        Authenticatable authenticatable = new Authenticatable() {};

        String authHeaderValue = authenticatable.getAuthHeaderValue();
        assertThat(authHeaderValue).startsWith("Bearer ");
    }
}
