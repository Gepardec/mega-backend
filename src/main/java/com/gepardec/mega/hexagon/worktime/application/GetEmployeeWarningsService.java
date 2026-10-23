package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.GetEmployeeWarningsUseCase;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeAbsenceZepPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeBookingZepPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeExpectedWorkingDaysPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeMealEntryZepPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeProjectSnapshotPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeUserSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeUserNotFoundException;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeValidationException;
import com.gepardec.mega.hexagon.worktime.domain.model.Absence;
import com.gepardec.mega.hexagon.worktime.domain.model.AbsenceType;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEvaluation;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.services.warning.WorkTimeWarningAssembler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class GetEmployeeWarningsService implements GetEmployeeWarningsUseCase {
    private final WorkTimeUserSnapshotPort userSnapshotPort;
    private final WorkTimeBookingZepPort bookingPort;
    private final WorkTimeAbsenceZepPort absencePort;
    private final WorkTimeExpectedWorkingDaysPort expectedWorkingDaysPort;
    private final WorkTimeProjectSnapshotPort projectSnapshotPort;
    private final WorkTimeMealEntryZepPort mealEntryPort;
    private final WorkTimeWarningAssembler assembler;
    private final Clock clock;

    @Inject
    public GetEmployeeWarningsService(WorkTimeUserSnapshotPort userSnapshotPort, WorkTimeBookingZepPort bookingPort,
                                      WorkTimeAbsenceZepPort absencePort, WorkTimeExpectedWorkingDaysPort expectedWorkingDaysPort,
                                      WorkTimeProjectSnapshotPort projectSnapshotPort, WorkTimeMealEntryZepPort mealEntryPort,
                                      Clock clock) {
        this(userSnapshotPort, bookingPort, absencePort, expectedWorkingDaysPort, projectSnapshotPort, mealEntryPort,
                new WorkTimeWarningAssembler(), clock);
    }

    GetEmployeeWarningsService(WorkTimeUserSnapshotPort userSnapshotPort, WorkTimeBookingZepPort bookingPort,
                               WorkTimeAbsenceZepPort absencePort, WorkTimeExpectedWorkingDaysPort expectedWorkingDaysPort,
                               WorkTimeProjectSnapshotPort projectSnapshotPort, WorkTimeMealEntryZepPort mealEntryPort,
                               WorkTimeWarningAssembler assembler, Clock clock) {
        this.userSnapshotPort = userSnapshotPort;
        this.bookingPort = bookingPort;
        this.absencePort = absencePort;
        this.expectedWorkingDaysPort = expectedWorkingDaysPort;
        this.projectSnapshotPort = projectSnapshotPort;
        this.mealEntryPort = mealEntryPort;
        this.assembler = assembler;
        this.clock = clock;
    }

    @Override
    public List<WorkTimeWarning> getWarnings(UserId employeeId, YearMonth month) {
        Objects.requireNonNull(employeeId, "employeeId must not be null");
        Objects.requireNonNull(month, "month must not be null");
        UserRef employee = userSnapshotPort.findById(employeeId, month)
                .orElseThrow(() -> new WorkTimeUserNotFoundException("user not found: " + employeeId.value()));
        if (employee.zepUsername() == null || employee.zepUsername().value().isBlank()) {
            throw new WorkTimeValidationException("zep username missing for user: " + employeeId.value());
        }

        WorkTimeBookings bookings = new WorkTimeBookings(
                bookingPort.fetchBookingsForEmployee(employee.zepUsername().value(), month)
        );
        LocalDate today = LocalDate.now(clock);
        if (bookings.isEmpty()) {
            return assembler.assemble(
                    new WorkTimeEvaluation(bookings, month, today, Set.of(), Set.of(), Set.of(), Set.of()));
        }
        Set<LocalDate> excusedDates = absencePort.fetchAbsencesForEmployee(employee.zepUsername(), month).stream()
                .filter(absence -> absence.type() != AbsenceType.HOME_OFFICE)
                .map(Absence::date)
                .collect(Collectors.toUnmodifiableSet());
        return assembler.assemble(new WorkTimeEvaluation(
                bookings,
                month,
                today,
                expectedWorkingDaysPort.expectedWorkingDays(employeeId, month),
                excusedDates,
                billableProjectZepIds(bookings),
                mealEntryDates(employee, bookings, month)));
    }

    private Set<LocalDate> mealEntryDates(UserRef employee, WorkTimeBookings bookings, YearMonth month) {
        if (bookings.journeys().isEmpty()) {
            return Set.of();
        }
        return mealEntryPort.fetchMealEntryDates(employee.zepUsername(), month);
    }

    private Set<Integer> billableProjectZepIds(WorkTimeBookings bookings) {
        Set<Integer> bookedProjectZepIds = bookings.values().stream()
                .map(WorkTimeBooking::projectZepId)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        return projectSnapshotPort.findBillableZepIds(bookedProjectZepIds);
    }
}
