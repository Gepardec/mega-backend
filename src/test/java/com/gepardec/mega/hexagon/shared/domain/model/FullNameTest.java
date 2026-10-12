package com.gepardec.mega.hexagon.shared.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FullNameTest {

    @Test
    void displayName_shouldJoinFirstnameAndLastname() {
        assertThat(FullName.of("Ada", "Lovelace").displayName()).isEqualTo("Ada Lovelace");
    }

    @Test
    void displayName_shouldReturnExistingPartWhenOtherPartIsMissing() {
        assertThat(FullName.of("Ada", null).displayName()).isEqualTo("Ada");
        assertThat(FullName.of(null, "Lovelace").displayName()).isEqualTo("Lovelace");
        assertThat(FullName.of(null, null).displayName()).isNull();
    }
}
