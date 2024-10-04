package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.dto.ZepAttendanceDirectionOfTravel;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectTimeMapperTest {

    @Test
    void withFullSettings_thenReturnsAbsenceTimeObject() {
        ProjectTimeMapper projectTimeMapper = new ProjectTimeMapper();
        ZepAttendanceDirectionOfTravel zepAttendanceDirectionOfTravel =
                ZepAttendanceDirectionOfTravel.builder()
                        .id("2")
                        .name("return")
                        .build();
        ZepAttendance zepAttendance = ZepAttendance.builder()
                .id(2)
                .date(LocalDate.of(2015, 2, 11))
                .from(LocalTime.of(9, 0, 0))
                .to(LocalTime.of(13, 45, 0))
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

        ProjectTime pt = projectTimeMapper.map(zepAttendance);

        assertThat(pt.getUserId()).isEqualTo(zepAttendance.employeeId());
        assertThat(pt.getDuration()).isEqualTo("04:45");
        assertThat(pt.getBillable()).isTrue();
    }

}

