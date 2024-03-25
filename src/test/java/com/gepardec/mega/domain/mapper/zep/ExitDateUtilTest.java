package com.gepardec.mega.domain.mapper.zep;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.utils.DateUtils;
import de.provantis.zep.BeschaeftigungszeitListeType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@QuarkusTest
public class ExitDateUtilTest {

    @Inject
    ExitDateUtil exitDateUtil;

    @Test
    public void getNewestExitDate_whenNull_thenReturnEmpty() {
        // When
        var result = exitDateUtil.getNewestExitDate(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void isActive_whenBeschaeftingungszeitListeIsNull_thenEmployeeIsInactive() {
        final de.provantis.zep.MitarbeiterType employee = new de.provantis.zep.MitarbeiterType();

        boolean isActive = exitDateUtil.isActive(employee.getBeschaeftigungszeitListe());

        Assertions.assertThat(isActive).isFalse();
    }

    @Test
    void isActive_whenBeschaeftingungszeitListeIsEmpty_thenEmployeeIsInactive() {
        var empty = new de.provantis.zep.BeschaeftigungszeitListeType();

        boolean isActive = exitDateUtil.isActive(empty);

        Assertions.assertThat(isActive).isFalse();
    }


    @Test
    void isActive_whenEmployeeWasEmployedInThePastOnce_thenEmployeeIsInactive() {
        final de.provantis.zep.BeschaeftigungszeitType closedEmployment = createBeschaeftigungszeitType(
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1)
        );
        final de.provantis.zep.BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(closedEmployment));

        boolean isActive = exitDateUtil.isActive(employments);
        Assertions.assertThat(isActive).isFalse();
    }

    @Test
    void isActive_whenEmployeeWasEmployedInThePastMultipleTimes_thenEmployeeIsInactive() {
        final de.provantis.zep.BeschaeftigungszeitType closedEmploymentOne = createBeschaeftigungszeitType(
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(8)
        );
        final de.provantis.zep.BeschaeftigungszeitType closedEmploymentTwo = createBeschaeftigungszeitType(LocalDate.now()
                .minusDays(7), LocalDate.now().minusDays(4));
        final de.provantis.zep.BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(closedEmploymentOne, closedEmploymentTwo));
        
        boolean isActive = exitDateUtil.isActive(employments);

        Assertions.assertThat(isActive).isFalse();
    }

    @Test
    void isActive_whenEmployeeWillBeEmployedInTheFutureWithOpenEnd_thenEmployeeIsInactive() {
        final de.provantis.zep.BeschaeftigungszeitType futureActiveEmployment = createBeschaeftigungszeitType(
                LocalDate.now().plusDays(1),
                null
        );
        final de.provantis.zep.BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(futureActiveEmployment));

        boolean isActive = exitDateUtil.isActive(employments);

        Assertions.assertThat(isActive).isFalse();
    }

    @Test
    void isActive_whenEmployeeWillBeEmployedInTheFutureWithFixedEnd_thenEmployeeIsInactive() {
        final de.provantis.zep.BeschaeftigungszeitType futureActiveEmployment = createBeschaeftigungszeitType(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2)
        );
        final de.provantis.zep.BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(futureActiveEmployment));

        boolean isActive = exitDateUtil.isActive(employments);

        Assertions.assertThat(isActive).isFalse();
    }

    @Test
    void isActive_whenEmployeeIsEmployedOneDayOnCurrentDay_thenEmployeeIsActive() {
        final LocalDate today = LocalDate.now();
        final de.provantis.zep.BeschaeftigungszeitType futureActiveEmployment = createBeschaeftigungszeitType(today, today);
        final de.provantis.zep.BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(futureActiveEmployment));

        boolean isActive = exitDateUtil.isActive(employments);

        Assertions.assertThat(isActive).isTrue();
    }

    @Test
    void isActive_whenEmployeeIsCurrentlyEmployedWithOpenEnd_thenEmployeeIsActive() {
        final de.provantis.zep.BeschaeftigungszeitType activeEmployment = createBeschaeftigungszeitType(
                LocalDate.now().minusDays(10),
                null
        );
        final de.provantis.zep.BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(activeEmployment));

        boolean isActive = exitDateUtil.isActive(employments);

        Assertions.assertThat(isActive).isTrue();
    }

    @Test
    void isActive_whenEmployeeIsCurrentlyEmployedWithFixedEndDate_thenEmployeeIsActive() {
        final de.provantis.zep.MitarbeiterType employee = new de.provantis.zep.MitarbeiterType();
        final de.provantis.zep.BeschaeftigungszeitType activeEmployment = createBeschaeftigungszeitType(
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(1)
        );
        final de.provantis.zep.BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(activeEmployment));

        boolean isActive = exitDateUtil.isActive(employments);

        Assertions.assertThat(isActive).isTrue();
    }

    @Test
    void isActive_whenEmployeeWasEmployedInThePastAndIsCurrentlyEmployed_thenEmployeeIsActive() {
        final de.provantis.zep.MitarbeiterType employee = new de.provantis.zep.MitarbeiterType();
        final de.provantis.zep.BeschaeftigungszeitType closedEmployment = createBeschaeftigungszeitType(
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(8)
        );
        final de.provantis.zep.BeschaeftigungszeitType activeEmployment = createBeschaeftigungszeitType(
                LocalDate.now().minusDays(7),
                LocalDate.now().plusDays(1)
        );
        final de.provantis.zep.BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(closedEmployment, activeEmployment));

        boolean isActive = exitDateUtil.isActive(employments);

        Assertions.assertThat(isActive).isTrue();
    }

    @Test
    void getNewestExitDate_NullParameter_NoExitDate() {
        // When
        Optional<LocalDate> exitDate = exitDateUtil.getNewestExitDate(null);

        // Then
        assertThat(exitDate).isEmpty();
    }

    @Test
    void getNewestExitDate_EmptyPeriodList_NoExitDate() {
         // Given
        var periods = new BeschaeftigungszeitListeType();

        // When
        Optional<LocalDate> exitDate = exitDateUtil.getNewestExitDate(periods);

        // Then
        assertThat(exitDate).isEmpty();
    }

    @Test
    void getNewestExitDate_AllPeriodsInFuture_NoExitDate() {
        // Given
        LocalDate now = LocalDate.now();
        BeschaeftigungszeitListeType beschaeftigungszeitListeType = new BeschaeftigungszeitListeType();
        List<de.provantis.zep.BeschaeftigungszeitType> employments = List.of(
            createBeschaeftigungszeitType(now.plusDays(1), now.plusDays(2)),
            createBeschaeftigungszeitType(now.plusDays(4), now.plusDays(22)),
            createBeschaeftigungszeitType(now.plusDays(11), now.plusDays(12))
        );
        beschaeftigungszeitListeType.setBeschaeftigungszeit(employments);

        // When
        Optional<LocalDate> exitDate = exitDateUtil.getNewestExitDate(beschaeftigungszeitListeType);

        // Then
        assertThat(exitDate).isEmpty();
    }

    @Test
    void getNewestExitDate_MultipleEmploymentsInThePast_ClosestExitDate() {
        // Given
        LocalDate now = LocalDate.now();
        LocalDate yesterday = now.minusDays(1);
        List<Range<LocalDate>> periods = new ArrayList<>();

        BeschaeftigungszeitListeType beschaeftigungszeitListeType = new BeschaeftigungszeitListeType();
        List<de.provantis.zep.BeschaeftigungszeitType> employments = List.of(
            createBeschaeftigungszeitType(now.minusDays(3), now.minusDays(2)),
            createBeschaeftigungszeitType(now.minusDays(4), now.minusDays(3)),
            createBeschaeftigungszeitType(now.minusDays(123), yesterday),
            createBeschaeftigungszeitType(now.minusDays(50), now.minusDays(12))
        );
        beschaeftigungszeitListeType.setBeschaeftigungszeit(employments);

        // When
        Optional<LocalDate> exitDate = exitDateUtil.getNewestExitDate(beschaeftigungszeitListeType);

        // Then
        assertThat(exitDate.get()).isEqualTo(yesterday);
    }

    @Test
    void getNewestExitDate_EmployeeHasResignedButTerminationDateInFuture_NoExitDate() {
        // Given
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfWork = now.minusYears(1);
        LocalDate lastDayOfWork = now.plusMonths(2);
        List<Range<LocalDate>> periods = new ArrayList<>();
        BeschaeftigungszeitListeType beschaeftigungszeitListeType = new BeschaeftigungszeitListeType();
        List<de.provantis.zep.BeschaeftigungszeitType> employments = List.of(
            createBeschaeftigungszeitType(firstDayOfWork, lastDayOfWork)
        );
        beschaeftigungszeitListeType.setBeschaeftigungszeit(employments);

        // When
        Optional<LocalDate> exitDate = exitDateUtil.getNewestExitDate(beschaeftigungszeitListeType);

        // Then
        assertThat(exitDate).isEmpty();
    }


    private de.provantis.zep.BeschaeftigungszeitType createBeschaeftigungszeitType(final LocalDate start, final LocalDate end) {
        final de.provantis.zep.BeschaeftigungszeitType beschaeftigung = new de.provantis.zep.BeschaeftigungszeitType();
        beschaeftigung.setStartdatum((start != null) ? DateUtils.formatDate(start) : null);
        beschaeftigung.setEnddatum((end != null) ? DateUtils.formatDate(end) : null);
        return beschaeftigung;
    }

    private de.provantis.zep.BeschaeftigungszeitListeType createBeschaeftigungszeitListeType(List<de.provantis.zep.BeschaeftigungszeitType> employments) {
        final de.provantis.zep.BeschaeftigungszeitListeType beschaeftigungszeitListeType = new de.provantis.zep.BeschaeftigungszeitListeType();
        beschaeftigungszeitListeType.getBeschaeftigungszeit().addAll(employments);
        return beschaeftigungszeitListeType;
    }

}
