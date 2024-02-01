package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.zep.mapper.ProjectTimeMapper;
import com.gepardec.mega.zep.rest.entity.ZepAttendance;
import de.provantis.zep.AttributeType;
import de.provantis.zep.AttributesType;
import de.provantis.zep.ProjektzeitType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AttendanceMapperTest {

    @Test
    void withFullSettings_thenReturnsAbsenceTimeObject() {
        AttendanceMapper attendanceMapper = new AttendanceMapper();
        ZepAttendance zepAttendance = ZepAttendance.builder()
                .id(2)
                .date(LocalDate.of(2015,2,11))
                .from(LocalTime.of(9,0,0))
                .to(LocalTime.of(13,45,0))
                .employeeId("001-duser")
                .projectId(1)
                .projectTaskId(2)
                .duration(4.75)
                .billable(2)
                .workLocation("HOME")
                .workLocationIsProjectRelevant(-1)
                .note("AttendanceServiceTest implementieren")
                .activity("bearbeiten")
                .start("Liesing bhf")
                .destination("Atzgersdorf bhf")
                .vehicle("Elektrotriebwagen 4020")
                .isPrivate(0)
                .passengers("2")
                .km("10")
                .directionOfTravel("Meidling")
                .ticketId("1")
                .subtaskId("1")
                .invoiceItemId("2")
                .created(LocalDateTime.of(2019,12,14,14,59,56))
                .modified(LocalDateTime.of(2019,12,27,11,6,39))
                .build();

        ProjectTime pt = attendanceMapper.map(zepAttendance);

        assertThat(pt.getId()).isEqualTo("" + zepAttendance.getId());
        assertThat(pt.getUserId()).isEqualTo(zepAttendance.getEmployeeId());
        assertThat(pt.getDate()).isEqualTo(zepAttendance.getDate() + "");
        assertThat(pt.getStartTime()).isEqualTo(zepAttendance.getFrom() + "");
        assertThat(pt.getEndTime()).isEqualTo(zepAttendance.getTo() + "");
        assertThat(pt.getDuration()).isEqualTo("04:45");
        assertThat(pt.getIsBillable()).isTrue();
        assertThat(pt.getIsLocationRelevantToProject()).isFalse();
        assertThat(pt.getLocation()).isEqualTo(zepAttendance.getWorkLocation());
        assertThat(pt.getComment()).isEqualTo(zepAttendance.getNote());
        assertThat(pt.getProjectNr()).isEqualTo("" + zepAttendance.getProjectId());
        assertThat(pt.getProcessNr()).isEqualTo(zepAttendance.getTicketId());
        assertThat(pt.getTask()).isEqualTo(zepAttendance.getProjectTaskId() + "");
        assertThat(pt.getStartLocation()).isEqualTo(zepAttendance.getStart());
        assertThat(pt.getEndLocation()).isEqualTo(zepAttendance.getDestination());
        assertThat(pt.getKm()).isEqualTo(Integer.parseInt(zepAttendance.getKm()));
        assertThat(pt.getAmountPassengers()).isEqualTo(Integer.parseInt(zepAttendance.getPassengers()));
        assertThat(pt.getVehicle()).isEqualTo(zepAttendance.getVehicle());
        assertThat(pt.getTicketNr()).isEqualTo(Integer.parseInt(zepAttendance.getTicketId()));
        assertThat(pt.getSubtaskNr()).isEqualTo(zepAttendance.getSubtaskId());
        assertThat(pt.getTravelDirection()).isEqualTo(zepAttendance.getDirectionOfTravel());
        assertThat(pt.getIsPrivateVehicle()).isEqualTo(false);
        assertThat(pt.getCreated()).isEqualTo(zepAttendance.getCreated() + "");
        assertThat(pt.getModified()).isEqualTo(zepAttendance.getModified() + "");
    }

}

