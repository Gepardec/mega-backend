package com.gepardec.mega.hexagon.shared.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ZepUsernameTest {

    @ParameterizedTest
    @CsvSource({
            "eworker, true",
            "worker, false",
            "Eworker, false"
    })
    void isExternal_shouldReturnExpectedValue(String username, boolean expected) {
        assertThat(ZepUsername.of(username).isExternal()).isEqualTo(expected);
    }
}
