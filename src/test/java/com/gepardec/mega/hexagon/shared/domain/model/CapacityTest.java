package com.gepardec.mega.hexagon.shared.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CapacityTest {

    @ParameterizedTest
    @CsvSource({
            "EMPLOYEE, EMPLOYEE",
            "CONTROLLING, PROJECT_LEAD"
    })
    void requiredRole_shouldMapCapacityToMatchingRole(Capacity capacity, Role expectedRole) {
        assertThat(capacity.requiredRole()).isEqualTo(expectedRole);
    }
}
