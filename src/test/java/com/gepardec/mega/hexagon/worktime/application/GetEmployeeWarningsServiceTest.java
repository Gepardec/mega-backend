package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeAbsenceZepPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeBookingZepPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeExpectedWorkingDaysPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeProjectSnapshotPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeUserSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeUserNotFoundException;
import com.gepardec.mega.hexagon.worktime.domain.model.Absence;
import com.gepardec.mega.hexagon.worktime.domain.model.AbsenceType;
import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
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
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
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
    WorkTimeProjectSnapshotPort projectPort;
    private GetEmployeeWarningsService service;

    @BeforeEach
    void setUp() {
        service = new GetEmployeeWarningsService(userPort, bookingPort, absencePort, expectedDaysPort, projectPort,
                new WorkTimeWarningAssembler(), Clock.fixed(Instant.parse("2026-05-10T00:00:00Z"), ZoneOffset.UTC));
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
    void getWarnings_prefixedBookingOnBillableProjectIsFlagged() {
        var date = MONTH.atDay(4);
        stubMonthWith(bookingOn(date, 1, "Intern - Teammeeting"));
        when(projectPort.findBillabilityByZepIds(Set.of(1))).thenReturn(Map.of(1, true));

        assertThat(service.getWarnings(USER_ID, MONTH))
                .contains(new WorkTimeWarning(date, WorkTimeWarningType.INTERNAL_PREFIX_ON_BILLABLE_PROJECT, null));
    }

    @Test
    void getWarnings_unprefixedBookingOnInternalProjectIsFlagged() {
        var date = MONTH.atDay(4);
        stubMonthWith(bookingOn(date, 2, "Teammeeting"));
        when(projectPort.findBillabilityByZepIds(Set.of(2))).thenReturn(Map.of(2, false));

        assertThat(service.getWarnings(USER_ID, MONTH))
                .contains(new WorkTimeWarning(date, WorkTimeWarningType.INTERNAL_PREFIX_MISSING, null));
    }

    @Test
    void getWarnings_bookingOnUnknownProjectIsNotCheckedAndPortReceivesDistinctIds() {
        var date = MONTH.atDay(4);
        stubMonthWith(
                bookingOn(date, 99, "Teammeeting"),
                bookingOn(MONTH.atDay(5), 99, "Intern - Teammeeting"));
        when(projectPort.findBillabilityByZepIds(Set.of(99))).thenReturn(Map.of());

        assertThat(service.getWarnings(USER_ID, MONTH)).extracting(WorkTimeWarning::type)
                .doesNotContain(WorkTimeWarningType.INTERNAL_PREFIX_MISSING, WorkTimeWarningType.INTERNAL_PREFIX_ON_BILLABLE_PROJECT);
        verify(projectPort).findBillabilityByZepIds(Set.of(99));
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

    private ProjectBooking bookingOn(LocalDate date) {
        return Instancio.of(ProjectBooking.class)
                .set(field(ProjectBooking::from), date.atTime(8, 0))
                .set(field(ProjectBooking::to), date.atTime(9, 0))
                .set(field(ProjectBooking::task), Task.BEARBEITEN)
                .set(field(ProjectBooking::workingLocation), WorkingLocation.MAIN)
                .create();
    }

    private void stubMonthWith(ProjectBooking... bookings) {
        when(userPort.findById(USER_ID, MONTH)).thenReturn(Optional.of(user()));
        when(bookingPort.fetchBookingsForEmployee("ada", MONTH)).thenReturn(List.of(bookings));
        when(absencePort.fetchAbsencesForEmployee(USERNAME, MONTH)).thenReturn(List.of());
        when(expectedDaysPort.expectedWorkingDays(USER_ID, MONTH)).thenReturn(Set.of());
    }

    private ProjectBooking bookingOn(LocalDate date, int projectZepId, String note) {
        return Instancio.of(ProjectBooking.class)
                .set(field(ProjectBooking::from), date.atTime(8, 0))
                .set(field(ProjectBooking::to), date.atTime(9, 0))
                .set(field(ProjectBooking::task), Task.BEARBEITEN)
                .set(field(ProjectBooking::workingLocation), WorkingLocation.MAIN)
                .set(field(ProjectBooking::projectZepId), projectZepId)
                .set(field(ProjectBooking::note), note)
                .create();
    }

    private UserRef user() {
        return new UserRef(USER_ID, FullName.of("Ada", "Lovelace"), USERNAME);
    }
}
