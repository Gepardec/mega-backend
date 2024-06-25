package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.dto.ZepAttendanceDirectionOfTravel;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@QuarkusTest
class AttendanceMapperTest {

    @Inject
    AttendanceMapper mapper;

    @InjectMock
    Logger logger;

    @Test
    void withFullSettings_thenReturnsAbsenceTimeObject() {
        ZepAttendanceDirectionOfTravel zepAttendanceDirectionOfTravel = ZepAttendanceDirectionOfTravel.builder()
                                                                                                    .id("2")
                                                                                                    .name("return")
                                                                                                    .build();
        ZepAttendance zepAttendance = ZepAttendance.builder()
                .id(2)
                .date(LocalDate.of(2015,2,11))
                .from(LocalTime.of(9,0,0))
                .to(LocalTime.of(13,45,0))
                .employeeId("001-duser")
                .projectId(1)
                .projectTaskId(2)
                .duration(4.75)
                .billable(true)
                .workLocation("HOME")
                .workLocationIsProjectRelevant(false)
                .activity("bearbeiten")
                .vehicle("Elektrotriebwagen 4020")
                .directionOfTravel(zepAttendanceDirectionOfTravel)
                .build();

        ProjectTime pt = mapper.map(zepAttendance);

        assertThat(pt.getUserId()).isEqualTo(zepAttendance.employeeId());
        assertThat(pt.getDuration()).isEqualTo("04:45");
        assertThat(pt.getBillable()).isTrue();
    }

    @Test
    void map_whenDurationIsNull_thenReturnsAbsenceTimeObject() {
        ZepAttendanceDirectionOfTravel zepAttendanceDirectionOfTravel = ZepAttendanceDirectionOfTravel.builder()
                .id("2")
                .name("return")
                .build();
        ZepAttendance zepAttendance = ZepAttendance.builder()
                .id(2)
                .date(LocalDate.of(2015,2,11))
                .from(LocalTime.of(9,0,0))
                .to(LocalTime.of(13,45,0))
                .employeeId("001-duser")
                .projectId(1)
                .projectTaskId(2)
                .duration(null)
                .billable(true)
                .workLocation("HOME")
                .workLocationIsProjectRelevant(false)
                .activity("bearbeiten")
                .vehicle("Elektrotriebwagen 4020")
                .directionOfTravel(zepAttendanceDirectionOfTravel)
                .build();

        ProjectTime pt = mapper.map(zepAttendance);

        assertThat(pt.getUserId()).isEqualTo(zepAttendance.employeeId());
        assertThat(pt.getDuration()).isNull();
        assertThat(pt.getBillable()).isTrue();
    }

    @Test
    void map_whenZepAttendanceIsNull_thenReturnNullAndLogMessage() {
        ProjectTime actual = mapper.map(null);

        assertThat(actual).isNull();
        verify(logger).info("ZEP REST implementation -- While trying to map ZepAttendance to ProjectTime, ZepAttendance was null");
    }

}

