package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.zep.rest.entity.ZepAbsence;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class AbsenceMapperTest {

    @Test
    public void mapZepAbsenceToAbsenceTime() {
        AbsenceMapper absenceMapper = new AbsenceMapper();
        ZepAbsence zepAbsence = ZepAbsence.builder()
                .id(1)
                .employeeId("001")
                .startDate(LocalDate.of(2019, 1, 2))
                .endDate(LocalDate.of(2019, 1, 5))
                .from(LocalTime.of(8, 1, 32))
                .to(LocalTime.of(17, 0, 0))
                .absenceReason("KR")
                .approved(true)
                .note("Extrauteringravidität")
                .timezone("UTC")
                .created("02-01-2019T08:01:32.0000Z")
                .modified("05-01-2019T18:11:12.0000Z")
                .build();

        AbsenceTime absence = absenceMapper.map(zepAbsence);

        assertThat(absence.getId()).isEqualTo(zepAbsence.getId());
        assertThat(absence.getUserId()).isEqualTo(zepAbsence.getEmployeeId());
        assertThat(absence.getFromDate()).isEqualTo(zepAbsence.getStartDate());
        assertThat(absence.getToDate()).isEqualTo(zepAbsence.getEndDate());
        assertThat(absence.getReason()).isEqualTo(zepAbsence.getAbsenceReason());
        assertThat(absence.getAccepted()).isEqualTo(zepAbsence.isApproved());
        assertThat(absence.getTimezone()).isEqualTo(zepAbsence.getTimezone());
        assertThat(absence.getCreated()).isEqualTo(zepAbsence.getCreated());
        assertThat(absence.getModified()).isEqualTo(zepAbsence.getModified());
    }

    @Test
    public void mapZepAbsencesToAbsenceTimes() {
        AbsenceMapper absenceMapper = new AbsenceMapper();

        ZepAbsence[] zepAbsencesArr = {
                ZepAbsence.builder()
                        .id(1)
                        .absenceReason("HO")
                        .startDate(LocalDate.of(2019, 1, 2))
                        .employeeId("1")
                        .build(),
                ZepAbsence.builder()
                        .id(2)
                        .absenceReason("KR")
                        .startDate(LocalDate.of(2019, 1, 2))
                        .employeeId("2")
                        .build(),
                ZepAbsence.builder()
                        .id(2)
                        .absenceReason("UB")
                        .startDate(LocalDate.of(2019, 1, 2))
                        .employeeId("3")
                        .build()

        };
        List<ZepAbsence> zepAbsences = List.of(zepAbsencesArr);

        List<AbsenceTime> absences =  absenceMapper.mapList(zepAbsences);
        Iterator<ZepAbsence> zepAbsencesIterator = zepAbsences.iterator();
        absences.forEach(absence -> {
            ZepAbsence zepAbsence = zepAbsencesIterator.next();
            assertThat(zepAbsence.getId()).isEqualTo(absence.getId());
            assertThat(zepAbsence.getAbsenceReason()).isEqualTo(absence.getReason());
            assertThat(zepAbsence.getEmployeeId()).isEqualTo(absence.getUserId());
            assertThat(zepAbsence.getStartDate()).isEqualTo(absence.getFromDate());
        });
    }
}
