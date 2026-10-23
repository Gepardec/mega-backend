package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
import com.gepardec.mega.hexagon.worktime.domain.model.Vehicle;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.dto.ZepAttendanceDirectionOfTravel;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkTimeBookingZepMapperTest {
    private final WorkTimeBookingZepMapper mapper = Mappers.getMapper(WorkTimeBookingZepMapper.class);

    @Test
    void toDomain_mapsProjectBooking() {
        var booking = (ProjectBooking) mapper.toDomain(base()
                .activity("BEARBEITEN")
                .workLocation("A")
                .workLocationIsProjectRelevant(true)
                .projectTaskId(233)
                .build());

        assertThat(booking.from()).isEqualTo(LocalDate.of(2026, 5, 4).atTime(8, 0));
        assertThat(booking.to()).isEqualTo(LocalDate.of(2026, 5, 4).atTime(9, 0));
        assertThat(booking.task()).isEqualTo(Task.BEARBEITEN);
        assertThat(booking.workingLocation()).isEqualTo(WorkingLocation.A);
        assertThat(booking.workLocationProjectRelevant()).isTrue();
        assertThat(booking.process()).isEqualTo("233");
    }

    @Test
    void toDomain_mapsJourneyAndDefaultsOptionalValues() {
        var booking = mapper.toDomain(base().activity("REISEN").vehicle("Auto").build());
        assertThat(booking).isInstanceOf(JourneyBooking.class);
        assertThat(((JourneyBooking) booking).direction()).isEqualTo(JourneyDirection.TO);
        assertThat(((JourneyBooking) booking).vehicle()).isEqualTo(Vehicle.CAR_ACTIVE);
        assertThat(booking.workingLocation()).isEqualTo(WorkingLocation.MAIN);
    }

    @Test
    void toDomain_mapsExplicitJourneyDirection() {
        var booking = (JourneyBooking) mapper.toDomain(base().activity("REISEN").vehicle("Auto")
                .directionOfTravel(new ZepAttendanceDirectionOfTravel("2", "Back")).build());
        assertThat(booking.direction()).isEqualTo(JourneyDirection.BACK);
    }

    @Test
    void toDomain_mapsBookingEndingAtMidnightToStartOfNextDay() {
        var booking = mapper.toDomain(base().activity("BEARBEITEN").projectTaskId(233)
                .from(LocalTime.of(18, 0)).to(LocalTime.MIDNIGHT).build());

        assertThat(booking.to()).isEqualTo(LocalDate.of(2026, 5, 5).atStartOfDay());
        assertThat(booking.date()).isEqualTo(LocalDate.of(2026, 5, 4));
        assertThat(booking.durationInHours()).isEqualTo(6d);
    }

    @Test
    void toDomain_carriesOverBillableFlagAndNote() {
        var project = mapper.toDomain(base().activity("BEARBEITEN").projectTaskId(233)
                .billable(true).note("Intern - JFX").build());
        var journey = mapper.toDomain(base().activity("REISEN").vehicle("Auto")
                .billable(true).note("Intern - JFX").build());

        assertThat(project.billable()).isTrue();
        assertThat(project.note()).isEqualTo("Intern - JFX");
        assertThat(journey.billable()).isTrue();
        assertThat(journey.note()).isEqualTo("Intern - JFX");
    }

    @Test
    void toDomain_treatsMissingBillableFlagAsNotBillable() {
        var project = mapper.toDomain(base().activity("BEARBEITEN").projectTaskId(233).build());
        var journey = mapper.toDomain(base().activity("REISEN").vehicle("Auto").build());

        assertThat(project.billable()).isFalse();
        assertThat(journey.billable()).isFalse();
    }

    @Test
    void toDomain_yieldsBookingWithoutNoteWhenNoteIsMissing() {
        var project = mapper.toDomain(base().activity("BEARBEITEN").projectTaskId(233).build());
        var journey = mapper.toDomain(base().activity("REISEN").vehicle("Auto").build());

        assertThat(project.note()).isNull();
        assertThat(journey.note()).isNull();
    }

    @Test
    void toDomain_carriesOverProjectIdAsProjectReference() {
        var project = mapper.toDomain(base().activity("BEARBEITEN").projectTaskId(233).projectId(42).build());
        var journey = mapper.toDomain(base().activity("REISEN").vehicle("Auto").projectId(42).build());

        assertThat(project.projectZepId()).isEqualTo(42);
        assertThat(journey.projectZepId()).isEqualTo(42);
    }

    @Test
    void toDomain_yieldsBookingWithoutProjectReferenceWhenProjectIdIsMissing() {
        var project = mapper.toDomain(base().activity("BEARBEITEN").projectTaskId(233).build());
        var journey = mapper.toDomain(base().activity("REISEN").vehicle("Auto").build());

        assertThat(project.projectZepId()).isNull();
        assertThat(journey.projectZepId()).isNull();
    }

    @Test
    void toDomain_carriesOverStartAndDestinationUnchanged() {
        var journey = (JourneyBooking) mapper.toDomain(base().activity("REISEN").vehicle("Auto")
                .start("Wien Hbf").destination("Linz").build());

        assertThat(journey.start()).isEqualTo("Wien Hbf");
        assertThat(journey.destination()).isEqualTo("Linz");
    }

    @Test
    void toDomain_yieldsJourneyWithoutStartAndDestinationWhenMissing() {
        var journey = (JourneyBooking) mapper.toDomain(base().activity("REISEN").vehicle("Auto").build());

        assertThat(journey.start()).isNull();
        assertThat(journey.destination()).isNull();
    }

    @Test
    void toDomain_rejectsUnknownRequiredValues() {
        ThrowableAssert.ThrowingCallable throwingCallable = () -> mapper.toDomain(base().activity("UNKNOWN").build());
        assertThatThrownBy(throwingCallable)
                .isInstanceOf(ZepServiceException.class);

        ThrowableAssert.ThrowingCallable throwingCallable1 = () -> mapper.toDomain(base().activity("REISEN").vehicle("bike").build());
        assertThatThrownBy(throwingCallable1)
                .isInstanceOf(ZepServiceException.class);
    }

    private ZepAttendance.Builder base() {
        return ZepAttendance.builder().date(LocalDate.of(2026, 5, 4))
                .from(LocalTime.of(8, 0)).to(LocalTime.of(9, 0));
    }
}
