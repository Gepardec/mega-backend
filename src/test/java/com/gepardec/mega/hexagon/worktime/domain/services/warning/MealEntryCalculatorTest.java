package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEvaluation;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection.BACK;
import static com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection.TO;
import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.MEAL_ENTRY_MISSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class MealEntryCalculatorTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);
    private static final LocalDate END_OF_MONTH_TODAY = LocalDate.of(2026, 10, 31);

    private final MealEntryCalculator calculator = new MealEntryCalculator();

    @Test
    void calculate_whenTripDayHasMealEntryWithAllMealsSet_thenNoWarning() {
        var bookings = bookings(journey(OCTOBER.atDay(14), TO), journey(OCTOBER.atDay(14), BACK));

        assertThat(calculate(bookings, OCTOBER, Set.of(OCTOBER.atDay(14)), END_OF_MONTH_TODAY)).isEmpty();
    }

    @Test
    void calculate_whenMealEntryExistsRegardlessOfMealFlags_thenNoWarning() {
        // the calculator only receives dates; an entry with no meal set yields its date like any other
        var bookings = bookings(journey(OCTOBER.atDay(13), TO), journey(OCTOBER.atDay(13), BACK));

        assertThat(calculate(bookings, OCTOBER, Set.of(OCTOBER.atDay(13)), END_OF_MONTH_TODAY)).isEmpty();
    }

    @Test
    void calculate_whenTripDayWithoutMealEntry_thenWarningWithoutHoursForThatDayOnly() {
        var bookings = bookings(journey(OCTOBER.atDay(12), TO), journey(OCTOBER.atDay(14), BACK));
        Set<LocalDate> mealEntries = Set.of(OCTOBER.atDay(12), OCTOBER.atDay(14));

        List<WorkTimeWarning> warnings = calculate(bookings, OCTOBER, mealEntries, END_OF_MONTH_TODAY);

        assertThat(warnings).containsExactly(new WorkTimeWarning(OCTOBER.atDay(13), MEAL_ENTRY_MISSING, null));
    }

    @Test
    void calculate_whenMealEntryOnlyOnNonTripDay_thenNoWarning() {
        var bookings = WorkTimeBookings.empty();

        assertThat(calculate(bookings, OCTOBER, Set.of(OCTOBER.atDay(20)), END_OF_MONTH_TODAY)).isEmpty();
    }

    @Test
    void calculate_whenMealEntriesOfOtherDates_thenTheyDoNotSatisfyTripDay() {
        var bookings = bookings(journey(OCTOBER.atDay(14), TO), journey(OCTOBER.atDay(14), BACK));

        List<WorkTimeWarning> warnings = calculate(bookings, OCTOBER, Set.of(OCTOBER.atDay(15)), END_OF_MONTH_TODAY);

        assertThat(warnings).containsExactly(new WorkTimeWarning(OCTOBER.atDay(14), MEAL_ENTRY_MISSING, null));
    }

    @Test
    void calculate_whenTripDayBeforeCutoff_thenNoWarning() {
        YearMonth september = YearMonth.of(2026, 9);
        var bookings = bookings(journey(september.atDay(30), TO), journey(september.atDay(30), BACK));

        assertThat(calculate(bookings, september, Set.of(), END_OF_MONTH_TODAY)).isEmpty();
    }

    @Test
    void calculate_whenTripDayOnCutoffDate_thenWarning() {
        var bookings = bookings(journey(OCTOBER.atDay(1), TO), journey(OCTOBER.atDay(1), BACK));

        List<WorkTimeWarning> warnings = calculate(bookings, OCTOBER, Set.of(), OCTOBER.atDay(2));

        assertThat(warnings).containsExactly(new WorkTimeWarning(OCTOBER.atDay(1), MEAL_ENTRY_MISSING, null));
    }

    @Test
    void calculate_whenTripDayIsToday_thenNoWarning() {
        var bookings = bookings(journey(OCTOBER.atDay(14), TO), journey(OCTOBER.atDay(14), BACK));

        assertThat(calculate(bookings, OCTOBER, Set.of(), OCTOBER.atDay(14))).isEmpty();
    }

    @Test
    void calculate_whenOpenTripExtendsPastToday_thenOnlyPastDaysAreChecked() {
        var bookings = bookings(journey(OCTOBER.atDay(27), TO));

        List<WorkTimeWarning> warnings = calculate(bookings, OCTOBER, Set.of(), OCTOBER.atDay(29));

        assertThat(warnings).extracting(WorkTimeWarning::date)
                .containsExactly(OCTOBER.atDay(27), OCTOBER.atDay(28));
    }

    private static WorkTimeBookings bookings(JourneyBooking... journeys) {
        return new WorkTimeBookings(List.of(journeys));
    }

    private static JourneyBooking journey(LocalDate date, JourneyDirection direction) {
        return Instancio.of(JourneyBooking.class)
                .set(field(JourneyBooking::from), date.atTime(direction == TO ? 8 : 16, 0))
                .set(field(JourneyBooking::to), date.atTime(direction == TO ? 9 : 17, 0))
                .set(field(JourneyBooking::direction), direction)
                .create();
    }

    private List<WorkTimeWarning> calculate(WorkTimeBookings bookings, YearMonth month, Set<LocalDate> mealEntryDates,
                                            LocalDate today) {
        return calculator.calculate(Instancio.of(WorkTimeEvaluation.class)
                .set(field(WorkTimeEvaluation::bookings), bookings)
                .set(field(WorkTimeEvaluation::payrollMonth), month)
                .set(field(WorkTimeEvaluation::mealEntryDates), mealEntryDates)
                .set(field(WorkTimeEvaluation::evaluationDate), today)
                .create());
    }
}
