package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WarningRuleSettingsPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeAbsenceZepPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeBookingZepPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeExpectedWorkingDaysPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeMealEntryZepPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeProjectSnapshotPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeUserSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeUserNotFoundException;
import com.gepardec.mega.hexagon.worktime.domain.model.Absence;
import com.gepardec.mega.hexagon.worktime.domain.model.AbsenceType;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRule;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRuleSetting;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;
import com.gepardec.mega.hexagon.worktime.domain.services.warning.WorkTimeWarningAssembler;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEmployeeWarningsServiceTest {
    private static final YearMonth MONTH = YearMonth.of(2026, 5);
    private static final UserId USER_ID = UserId.of(Instancio.create(UUID.class));
    private static final ZepUsername USERNAME = ZepUsername.of("ada");

    @Mock
    WorkTimeUserSnapshotPort userPort;
    @Mock
    WorkTimeBookingZepPort bookingPort;
    @Mock
    WorkTimeAbsenceZepPort absencePort;
    @Mock
    WorkTimeExpectedWorkingDaysPort expectedDaysPort;
    @Mock
    WorkTimeProjectSnapshotPort projectSnapshotPort;
    @Mock
    WorkTimeMealEntryZepPort mealEntryPort;
    @Mock
    WarningRuleSettingsPort ruleSettingsPort;
    private GetEmployeeWarningsService service;

    @BeforeEach
    void setUp() {
        lenient().when(ruleSettingsPort.findAll()).thenReturn(settings(Set.of()));
        service = new GetEmployeeWarningsService(userPort, bookingPort, absencePort, expectedDaysPort,
                projectSnapshotPort, mealEntryPort, ruleSettingsPort, new WorkTimeWarningAssembler(), Clock.fixed(Instant.parse("2026-05-10T00:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void getWarnings_homeOfficeAbsenceDoesNotExcuseMissingEntry() {
        var missing = MONTH.atDay(4);
        var booked = MONTH.atDay(5);
        when(userPort.findById(USER_ID, MONTH)).thenReturn(Optional.of(user()));
        when(bookingPort.fetchBookingsForEmployee("ada", MONTH)).thenReturn(List.of(bookingOn(booked)));
        when(absencePort.fetchAbsencesForEmployee(USERNAME, MONTH)).thenReturn(List.of(
                new Absence(missing, AbsenceType.HOME_OFFICE)));
        when(expectedDaysPort.expectedWorkingDays(USER_ID, MONTH)).thenReturn(Set.of(missing, booked));

        assertThat(service.getWarnings(USER_ID, MONTH))
                .contains(new WorkTimeWarning(missing, WorkTimeWarningType.NO_TIME_ENTRY, null));
    }

    @Test
    void getWarnings_nonHomeOfficeAbsenceExcusesMissingEntry() {
        var excused = MONTH.atDay(4);
        var booked = MONTH.atDay(5);
        when(userPort.findById(USER_ID, MONTH)).thenReturn(Optional.of(user()));
        when(bookingPort.fetchBookingsForEmployee("ada", MONTH)).thenReturn(List.of(bookingOn(booked)));
        when(absencePort.fetchAbsencesForEmployee(USERNAME, MONTH)).thenReturn(List.of(
                new Absence(excused, AbsenceType.VACATION)));
        when(expectedDaysPort.expectedWorkingDays(USER_ID, MONTH)).thenReturn(Set.of(excused, booked));

        assertThat(service.getWarnings(USER_ID, MONTH)).extracting(WorkTimeWarning::type)
                .doesNotContain(WorkTimeWarningType.NO_TIME_ENTRY);
    }

    @Test
    void getWarnings_excludesCurrentAndLaterDaysUsingInjectedClock() {
        var missing = MONTH.atDay(4);
        var booked = MONTH.atDay(5);
        var today = MONTH.atDay(10);
        var future = MONTH.atDay(11);
        when(userPort.findById(USER_ID, MONTH)).thenReturn(Optional.of(user()));
        when(bookingPort.fetchBookingsForEmployee("ada", MONTH)).thenReturn(List.of(bookingOn(booked)));
        when(absencePort.fetchAbsencesForEmployee(USERNAME, MONTH)).thenReturn(List.of());
        when(expectedDaysPort.expectedWorkingDays(USER_ID, MONTH)).thenReturn(Set.of(missing, booked, today, future));

        assertThat(service.getWarnings(USER_ID, MONTH))
                .filteredOn(warning -> warning.type() == WorkTimeWarningType.NO_TIME_ENTRY)
                .extracting(WorkTimeWarning::date)
                .containsExactly(missing);
    }

    @Test
    void getWarnings_unknownEmployeeThrowsNotFound() {
        when(userPort.findById(USER_ID, MONTH)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getWarnings(USER_ID, MONTH)).isInstanceOf(WorkTimeUserNotFoundException.class);
    }

    @Test
    void getWarnings_emptyMonthShortCircuitsExternalAbsenceAndExpectedDayFetches() {
        when(userPort.findById(USER_ID, MONTH)).thenReturn(Optional.of(user()));
        when(bookingPort.fetchBookingsForEmployee("ada", MONTH)).thenReturn(List.of());
        assertThat(service.getWarnings(USER_ID, MONTH)).singleElement()
                .extracting("type").isEqualTo(WorkTimeWarningType.EMPTY_ENTRY_LIST);
    }

    @Test
    void getWarnings_emptyMonthDoesNotQueryProjectBillability() {
        when(userPort.findById(USER_ID, MONTH)).thenReturn(Optional.of(user()));
        when(bookingPort.fetchBookingsForEmployee("ada", MONTH)).thenReturn(List.of());

        service.getWarnings(USER_ID, MONTH);

        verify(projectSnapshotPort, never()).findBillableZepIds(anySet());
    }

    @Test
    void getWarnings_passesBillableProjectIdsOfBookedProjectsToAssembler() {
        YearMonth october = YearMonth.of(2026, 10);
        var date = october.atDay(6);
        when(userPort.findById(USER_ID, october)).thenReturn(Optional.of(user()));
        when(bookingPort.fetchBookingsForEmployee("ada", october)).thenReturn(List.of(
                nonBillableBookingOn(date, 42), nonBillableBookingOn(date, 42), nonBillableBookingOn(date, null)));
        when(absencePort.fetchAbsencesForEmployee(USERNAME, october)).thenReturn(List.of());
        when(expectedDaysPort.expectedWorkingDays(USER_ID, october)).thenReturn(Set.of(date));
        when(projectSnapshotPort.findBillableZepIds(Set.of(42))).thenReturn(Set.of(42));

        assertThat(service.getWarnings(USER_ID, october))
                .contains(new WorkTimeWarning(date, WorkTimeWarningType.NON_BILLABLE_ON_BILLABLE_PROJECT, null));
    }

    @Test
    void getWarnings_bookingOnUnknownProjectDoesNotFailTheRequest() {
        YearMonth october = YearMonth.of(2026, 10);
        var date = october.atDay(6);
        when(userPort.findById(USER_ID, october)).thenReturn(Optional.of(user()));
        when(bookingPort.fetchBookingsForEmployee("ada", october)).thenReturn(List.of(nonBillableBookingOn(date, 99)));
        when(absencePort.fetchAbsencesForEmployee(USERNAME, october)).thenReturn(List.of());
        when(expectedDaysPort.expectedWorkingDays(USER_ID, october)).thenReturn(Set.of(date));
        when(projectSnapshotPort.findBillableZepIds(Set.of(99))).thenReturn(Set.of());

        assertThat(service.getWarnings(USER_ID, october)).extracting(WorkTimeWarning::type)
                .doesNotContain(WorkTimeWarningType.NON_BILLABLE_ON_BILLABLE_PROJECT);
    }

    @Test
    void getWarnings_tripDayWithoutMealEntryYieldsMealEntryMissing() {
        YearMonth october = YearMonth.of(2026, 10);
        var date = october.atDay(6);
        stubMonthWithDayTrip(october, date);
        when(mealEntryPort.fetchMealEntryDates(USERNAME, october)).thenReturn(Set.of());

        assertThat(serviceAt("2026-10-10T00:00:00Z").getWarnings(USER_ID, october))
                .contains(new WorkTimeWarning(date, WorkTimeWarningType.MEAL_ENTRY_MISSING, null));
    }

    @Test
    void getWarnings_tripDayWithMealEntryYieldsNoMealEntryMissing() {
        YearMonth october = YearMonth.of(2026, 10);
        var date = october.atDay(6);
        stubMonthWithDayTrip(october, date);
        when(mealEntryPort.fetchMealEntryDates(USERNAME, october)).thenReturn(Set.of(date));

        assertThat(serviceAt("2026-10-10T00:00:00Z").getWarnings(USER_ID, october)).extracting(WorkTimeWarning::type)
                .doesNotContain(WorkTimeWarningType.MEAL_ENTRY_MISSING);
    }

    @Test
    void getWarnings_monthWithoutJourneysDoesNotQueryMealEntries() {
        when(userPort.findById(USER_ID, MONTH)).thenReturn(Optional.of(user()));
        when(bookingPort.fetchBookingsForEmployee("ada", MONTH)).thenReturn(List.of(bookingOn(MONTH.atDay(5))));
        when(absencePort.fetchAbsencesForEmployee(USERNAME, MONTH)).thenReturn(List.of());
        when(expectedDaysPort.expectedWorkingDays(USER_ID, MONTH)).thenReturn(Set.of());

        service.getWarnings(USER_ID, MONTH);

        verify(mealEntryPort, never()).fetchMealEntryDates(any(), any());
    }

    @Test
    void getWarnings_disabledMealEntryRuleProducesNoWarningsAndSkipsMealEntryFetch() {
        YearMonth october = YearMonth.of(2026, 10);
        var date = october.atDay(6);
        stubMonthWithDayTrip(october, date);
        when(ruleSettingsPort.findAll()).thenReturn(settings(Set.of(), WarningRule.MEAL_ENTRY));

        assertThat(serviceAt("2026-10-10T00:00:00Z").getWarnings(USER_ID, october)).extracting(WorkTimeWarning::type)
                .doesNotContain(WorkTimeWarningType.MEAL_ENTRY_MISSING);
        verify(mealEntryPort, never()).fetchMealEntryDates(any(), any());
    }

    @Test
    void getWarnings_rulesNeedingBillabilityDisabled_skipsBillableProjectLookup() {
        when(userPort.findById(USER_ID, MONTH)).thenReturn(Optional.of(user()));
        when(bookingPort.fetchBookingsForEmployee("ada", MONTH)).thenReturn(List.of(nonBillableBookingOn(MONTH.atDay(5), 42)));
        when(absencePort.fetchAbsencesForEmployee(USERNAME, MONTH)).thenReturn(List.of());
        when(expectedDaysPort.expectedWorkingDays(USER_ID, MONTH)).thenReturn(Set.of());
        when(ruleSettingsPort.findAll()).thenReturn(settings(Set.of(),
                WarningRule.BILLABLE_PROJECT, WarningRule.INTERNAL_NOTE, WarningRule.TRAVEL_ALLOWANCE));

        service.getWarnings(USER_ID, MONTH);

        verify(projectSnapshotPort, never()).findBillableZepIds(anySet());
    }

    @Test
    void getWarnings_emptyMonthDoesNotLoadRuleSettings() {
        when(userPort.findById(USER_ID, MONTH)).thenReturn(Optional.of(user()));
        when(bookingPort.fetchBookingsForEmployee("ada", MONTH)).thenReturn(List.of());

        service.getWarnings(USER_ID, MONTH);

        verify(ruleSettingsPort, never()).findAll();
    }

    @Test
    void getWarnings_missingSettingTreatsRuleAsDisabledAndKeepsOtherWarnings() {
        var missing = MONTH.atDay(4);
        var booked = MONTH.atDay(5);
        when(userPort.findById(USER_ID, MONTH)).thenReturn(Optional.of(user()));
        when(bookingPort.fetchBookingsForEmployee("ada", MONTH)).thenReturn(List.of(bookingOn(booked)));
        when(absencePort.fetchAbsencesForEmployee(USERNAME, MONTH)).thenReturn(List.of());
        when(expectedDaysPort.expectedWorkingDays(USER_ID, MONTH)).thenReturn(Set.of(missing, booked));
        when(ruleSettingsPort.findAll()).thenReturn(settings(Set.of(WarningRule.MEAL_ENTRY)));

        assertThat(service.getWarnings(USER_ID, MONTH))
                .contains(new WorkTimeWarning(missing, WorkTimeWarningType.NO_TIME_ENTRY, null));
    }

    private static List<WarningRuleSetting> settings(Set<WarningRule> withoutSetting, WarningRule... disabled) {
        Set<WarningRule> disabledRules = Set.of(disabled);
        Set<WarningRule> dated = Set.of(WarningRule.BILLABLE_PROJECT, WarningRule.INTERNAL_NOTE,
                WarningRule.MEAL_ENTRY, WarningRule.TRAVEL_ALLOWANCE);
        return Arrays.stream(WarningRule.values())
                .filter(rule -> !withoutSetting.contains(rule))
                .map(rule -> new WarningRuleSetting(rule, !disabledRules.contains(rule),
                        dated.contains(rule) ? LocalDate.of(2026, 10, 1) : null))
                .toList();
    }

    private GetEmployeeWarningsService serviceAt(String instant) {
        return new GetEmployeeWarningsService(userPort, bookingPort, absencePort, expectedDaysPort,
                projectSnapshotPort, mealEntryPort, ruleSettingsPort, new WorkTimeWarningAssembler(), Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));
    }

    private void stubMonthWithDayTrip(YearMonth month, LocalDate date) {
        when(userPort.findById(USER_ID, month)).thenReturn(Optional.of(user()));
        when(bookingPort.fetchBookingsForEmployee("ada", month)).thenReturn(List.of(
                journeyOn(date.atTime(8, 0), JourneyDirection.TO), journeyOn(date.atTime(16, 0), JourneyDirection.BACK)));
        when(absencePort.fetchAbsencesForEmployee(USERNAME, month)).thenReturn(List.of());
        when(expectedDaysPort.expectedWorkingDays(USER_ID, month)).thenReturn(Set.of(date));
        when(projectSnapshotPort.findBillableZepIds(anySet())).thenReturn(Set.of());
    }

    private JourneyBooking journeyOn(LocalDateTime from, JourneyDirection direction) {
        return Instancio.of(JourneyBooking.class)
                .set(field(JourneyBooking::from), from)
                .set(field(JourneyBooking::to), from.plusHours(1))
                .set(field(JourneyBooking::direction), direction)
                .create();
    }

    private ProjectBooking nonBillableBookingOn(LocalDate date, Integer projectZepId) {
        return Instancio.of(ProjectBooking.class)
                .set(field(ProjectBooking::from), date.atTime(8, 0))
                .set(field(ProjectBooking::to), date.atTime(9, 0))
                .set(field(ProjectBooking::task), Task.BEARBEITEN)
                .set(field(ProjectBooking::workingLocation), WorkingLocation.MAIN)
                .set(field(ProjectBooking::billable), false)
                .set(field(ProjectBooking::note), "Intern - Test")
                .set(field(ProjectBooking::projectZepId), projectZepId)
                .create();
    }

    private ProjectBooking bookingOn(LocalDate date) {
        return Instancio.of(ProjectBooking.class)
                .set(field(ProjectBooking::from), date.atTime(8, 0))
                .set(field(ProjectBooking::to), date.atTime(9, 0))
                .set(field(ProjectBooking::task), Task.BEARBEITEN)
                .set(field(ProjectBooking::workingLocation), WorkingLocation.MAIN)
                .set(field(ProjectBooking::billable), true)
                .set(field(ProjectBooking::note), "Test")
                .create();
    }

    private UserRef user() {
        return new UserRef(USER_ID, FullName.of("Ada", "Lovelace"), USERNAME);
    }
}
