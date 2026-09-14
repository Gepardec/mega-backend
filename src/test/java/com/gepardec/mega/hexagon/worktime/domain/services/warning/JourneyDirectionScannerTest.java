package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection.BACK;
import static com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection.FURTHER;
import static com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection.TO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class JourneyDirectionScannerTest {

    private JourneyDirectionScanner scanner;

    @BeforeEach
    void beforeEach() {
        scanner = new JourneyDirectionScanner();
    }

    @Test
    void whenValidJourneysWithDirectionFurther_thenReturnsNoWarnings() {
        assertAll(
                () -> assertThat(scanner.advance(TO, FURTHER)).isNull(),
                () -> assertThat(scanner.advance(FURTHER, FURTHER)).isNull(),
                () -> assertThat(scanner.advance(FURTHER, BACK)).isNull(),
                () -> assertThat(scanner.advance(BACK, null)).isNull());
    }

    @Test
    void whenValidJourneysStartedAndFinished_thenReturnsNoWarnings() {
        assertAll(
                () -> assertThat(scanner.advance(TO, BACK)).isNull(),
                () -> assertThat(scanner.advance(BACK, null)).isNull());
    }

    @Test
    void whenOnlyJourneyStarted_thenReturnsNoWarning() {
        assertThat(scanner.advance(TO, null)).isEqualTo(WorkTimeWarningType.BACK_MISSING);
    }

    @Test
    void whenOnlyJourneyFurther_thenReturnsWarningJourneyToMissing() {
        assertThat(scanner.advance(FURTHER, null)).isEqualTo(WorkTimeWarningType.TO_MISSING);
    }

    @Test
    void whenOnlyJourneyFinished_thenReturnsWarningJourneyToMissing() {
        assertThat(scanner.advance(BACK, null)).isEqualTo(WorkTimeWarningType.TO_MISSING);
    }

    @Test
    void whenFirstJourneyNotFinishedAndNewJourneyStarted_thenReturnsTwoTimesWarningJourneyBackMissing() {
        assertAll(
                () -> assertThat(scanner.advance(TO, TO)).isEqualTo(WorkTimeWarningType.BACK_MISSING),
                () -> assertThat(scanner.advance(TO, null)).isEqualTo(WorkTimeWarningType.BACK_MISSING));
    }

    @Test
    void whenFirstJourneyNotStartedAndSecondReturnsTrue_thenOnFirstJourneyReturnsJourneyToMissingWarning() {
        assertAll(
                () -> assertThat(scanner.advance(BACK, TO)).isEqualTo(WorkTimeWarningType.TO_MISSING),
                () -> assertThat(scanner.advance(TO, BACK)).isNull(),
                () -> assertThat(scanner.advance(BACK, null)).isNull());
    }

    @Test
    void whenFirstJourneyNotStartedAndNotFinishedAndSecondReturnsTrue_thenOnFirstJourneyReturnsJourneyToMissingWarning() {
        assertAll(
                () -> assertThat(scanner.advance(FURTHER, TO)).isEqualTo(WorkTimeWarningType.TO_MISSING),
                () -> assertThat(scanner.advance(TO, BACK)).isNull(),
                () -> assertThat(scanner.advance(BACK, null)).isNull());
    }

    @Test
    void whenFirstJourneyOnlyFinishedAndSecondJourneyIsValid_thenOnFirstJourneyReturnsJourneyToMissingWarning() {
        assertAll(
                () -> assertThat(scanner.advance(BACK, TO)).isEqualTo(WorkTimeWarningType.TO_MISSING),
                () -> assertThat(scanner.advance(TO, BACK)).isNull(),
                () -> assertThat(scanner.advance(BACK, null)).isNull());
    }

    @Test
    void whenFirstJourneyIsValidAndSecondJourneyIsStarted_thenOnLastJourneyReturnsNoWarning() {
        assertAll(
                () -> assertThat(scanner.advance(TO, BACK)).isNull(),
                () -> assertThat(scanner.advance(BACK, TO)).isNull(),
                () -> assertThat(scanner.advance(TO, null)).isEqualTo(WorkTimeWarningType.BACK_MISSING));
    }

    @Test
    void whenFirstJourneyValidAndSecondJourneyIsNotStartedAndFinished_thenOnLastJourneyReturnsJourneyToMissingWarning() {
        assertAll(
                () -> assertThat(scanner.advance(TO, BACK)).isNull(),
                () -> assertThat(scanner.advance(BACK, FURTHER)).isNull(),
                () -> assertThat(scanner.advance(FURTHER, null)).isEqualTo(WorkTimeWarningType.TO_MISSING));
    }

    @Test
    void whenFirstJourneyValidAndSecondJourneyIsNotStarted_thenOnLastJourneyReturnsJourneyToMissingWarning() {
        assertAll(
                () -> assertThat(scanner.advance(TO, BACK)).isNull(),
                () -> assertThat(scanner.advance(BACK, BACK)).isNull(),
                () -> assertThat(scanner.advance(BACK, null)).isEqualTo(WorkTimeWarningType.TO_MISSING));
    }

    @Test
    void whenFirstJourneyIsRunningAndSecondJourneyIsStarted_thenOnSecondJourneyReturnsJourneyBackMissingWarning() {
        assertAll(
                () -> assertThat(scanner.advance(TO, FURTHER)).isNull(),
                () -> assertThat(scanner.advance(FURTHER, TO)).isEqualTo(WorkTimeWarningType.BACK_MISSING),
                () -> assertThat(scanner.advance(TO, null)).isEqualTo(WorkTimeWarningType.BACK_MISSING));
    }

    @Test
    void whenMultipleValidAndInvalidJourneys_thenReturnsCorrespondingWarnings() {
        assertAll(
                () -> assertThat(scanner.advance(BACK, TO)).isEqualTo(WorkTimeWarningType.TO_MISSING),
                () -> assertThat(scanner.advance(TO, BACK)).isNull(),
                () -> assertThat(scanner.advance(BACK, TO)).isNull(),
                () -> assertThat(scanner.advance(TO, TO)).isEqualTo(WorkTimeWarningType.BACK_MISSING),
                () -> assertThat(scanner.advance(TO, BACK)).isNull(),
                () -> assertThat(scanner.advance(BACK, BACK)).isNull(),
                () -> assertThat(scanner.advance(BACK, null)).isEqualTo(WorkTimeWarningType.TO_MISSING));
    }
}
