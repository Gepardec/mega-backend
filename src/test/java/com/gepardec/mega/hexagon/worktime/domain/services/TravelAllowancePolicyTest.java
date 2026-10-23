package com.gepardec.mega.hexagon.worktime.domain.services;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.Vehicle;
import org.instancio.Instancio;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class TravelAllowancePolicyTest {

    @ParameterizedTest
    @CsvSource({
            "St. Pölten, Wien, 15",
            "Linz, St. Pölten, 15",
            "Wien Hbf, St. Pölten Hbf, 15",
            "St. Pölten, Linz, 15",
            "St. Pölten, Salzburg, 30",
            "Wien, Linz, 30",
            "St. Pölten, Irgendwo, 30",
            "Wien, Wien, 30",
            "St. Pölten, St. Pölten, 30"
    })
    void allowanceFor_whenPassive_thenAllowanceByRoute(String start, String destination, long expectedMinutes) {
        assertThat(TravelAllowancePolicy.allowanceFor(journey(Vehicle.CAR_INACTIVE, start, destination)))
                .isEqualTo(Duration.ofMinutes(expectedMinutes));
    }

    @ParameterizedTest
    @CsvSource(value = {"NIL, Wien", "Wien, NIL", "NIL, NIL"}, nullValues = "NIL")
    void allowanceFor_whenStartOrDestinationMissing_thenOrdinaryRoute(String start, String destination) {
        assertThat(TravelAllowancePolicy.allowanceFor(journey(Vehicle.OTHER_INACTIVE, start, destination)))
                .isEqualTo(Duration.ofMinutes(30));
    }

    @ParameterizedTest
    @CsvSource({"St. Pölten, Wien", "Wien, Linz"})
    void allowanceFor_whenActive_thenZero(String start, String destination) {
        assertThat(TravelAllowancePolicy.allowanceFor(journey(Vehicle.CAR_ACTIVE, start, destination)))
                .isEqualTo(Duration.ZERO);
    }

    private static JourneyBooking journey(Vehicle vehicle, String start, String destination) {
        return Instancio.of(JourneyBooking.class)
                .set(field(JourneyBooking::vehicle), vehicle)
                .set(field(JourneyBooking::start), start)
                .set(field(JourneyBooking::destination), destination)
                .create();
    }
}
