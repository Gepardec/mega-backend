package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.zep.mapper.ProjectTimeMapper;
import com.gepardec.mega.zep.rest.entity.ZepAttendance;
import com.gepardec.mega.zep.rest.entity.ZepBillable;
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
                .billable(ZepBillable.builder().id(1).build())
                .workLocation("HOME")
                .workLocationIsProjectRelevant(false)
                .activity("bearbeiten")
                .vehicle("Elektrotriebwagen 4020")
                .directionOfTravel("Meidling")
                .build();

        ProjectTime pt = attendanceMapper.map(zepAttendance);

        assertThat(pt.getUserId()).isEqualTo(zepAttendance.employeeId());
        assertThat(pt.getDuration()).isEqualTo("04:45");
        assertThat(pt.getBillable()).isTrue();
    }

}

