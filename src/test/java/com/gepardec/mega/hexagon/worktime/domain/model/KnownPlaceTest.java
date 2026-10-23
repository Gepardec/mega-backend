package com.gepardec.mega.hexagon.worktime.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class KnownPlaceTest {

    @ParameterizedTest
    @CsvSource({
            "Wien, WIEN",
            "WIEN, WIEN",
            "Wien Hbf, WIEN",
            "Wien-Meidling, WIEN",
            "Linz, LINZ",
            "linz hbf, LINZ",
            "St. Pölten, ST_POELTEN",
            "St.Pölten, ST_POELTEN",
            "Sankt Pölten, ST_POELTEN",
            "st poelten, ST_POELTEN",
            "STPOELTEN, ST_POELTEN",
            "St. Pölten Hbf, ST_POELTEN"
    })
    void recognise_whenTextNamesOneKnownPlace_thenPlace(String text, KnownPlace expected) {
        assertThat(KnownPlace.recognise(text)).contains(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Wiener Neustadt", "Linzer Straße", "Salzburg", "Graz Hbf"})
    void recognise_whenTextIsNoWholeWordMatch_thenEmpty(String text) {
        assertThat(KnownPlace.recognise(text)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"St. Pölten / Wien", "Wien - Linz"})
    void recognise_whenTextNamesSeveralPlaces_thenEmpty(String text) {
        assertThat(KnownPlace.recognise(text)).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    void recognise_whenTextIsMissingOrBlank_thenEmpty(String text) {
        assertThat(KnownPlace.recognise(text)).isEmpty();
    }

    @Test
    void recognise_whenSameWordRepeated_thenStillMatches() {
        assertThat(KnownPlace.recognise("Wien Wien")).contains(KnownPlace.WIEN);
    }
}
