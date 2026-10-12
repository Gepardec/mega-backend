package com.gepardec.mega.hexagon.user.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HourlyRateTest {

    @ParameterizedTest
    @ValueSource(doubles = {0.01d, 1.0d, 999.99d})
    void of_shouldCreateHourlyRate_whenValueIsPositive(double value) {
        HourlyRate hourlyRate = HourlyRate.of(value);

        assertThat(hourlyRate.value()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0d, -0.01d, -100.0d})
    void of_shouldRejectHourlyRate_whenValueIsNotPositive(double value) {
        assertThatThrownBy(() -> HourlyRate.of(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("hourlyRate must be greater than zero");
    }
}
