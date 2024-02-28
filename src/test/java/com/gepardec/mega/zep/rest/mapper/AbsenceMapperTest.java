package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.zep.rest.entity.ZepAbsence;
import com.gepardec.mega.zep.rest.entity.ZepAbsenceReason;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class AbsenceMapperTest {

    @Inject
    AbsenceMapper absenceMapper;

    @Test
    public void mapZepAbsenceToAbsenceTime() {
        ZepAbsenceReason zepAbsenceReason = ZepAbsenceReason.builder().name("KR").build();
        ZepAbsence zepAbsence = ZepAbsence.builder()
                .id(1)
                .employeeId("001")
                .startDate(LocalDate.of(2019, 1, 2))
                .endDate(LocalDate.of(2019, 1, 5))
                .from(LocalTime.of(8, 1, 32))
                .to(LocalTime.of(17, 0, 0))
                .absenceReason(zepAbsenceReason)
                .approved(true)
                .note("Extrauteringravidität")
                .timezone("UTC")
                .created("02-01-2019T08:01:32.0000Z")
                .modified("05-01-2019T18:11:12.0000Z")
                .build();

        AbsenceTime absence = absenceMapper.map(zepAbsence);

        assertThat(absence.getId()).isEqualTo(zepAbsence.id());
        assertThat(absence.getUserId()).isEqualTo(zepAbsence.employeeId());
        assertThat(absence.getFromDate()).isEqualTo(zepAbsence.startDate());
        assertThat(absence.getToDate()).isEqualTo(zepAbsence.endDate());
        assertThat(absence.getReason()).isEqualTo(zepAbsence.absenceReason().name());
        assertThat(absence.getAccepted()).isEqualTo(zepAbsence.approved());
        assertThat(absence.getTimezone()).isEqualTo(zepAbsence.timezone());
        assertThat(absence.getCreated()).isEqualTo(zepAbsence.created());
        assertThat(absence.getModified()).isEqualTo(zepAbsence.modified());
    }

    @Test
    public void mapZepAbsencesToAbsenceTimes() {


        ZepAbsence[] zepAbsencesArr = {
                ZepAbsence.builder()
                        .id(1)
                        .absenceReason(ZepAbsenceReason.builder().name("KR").build())
                        .startDate(LocalDate.of(2019, 1, 2))
                        .employeeId("1")
                        .build(),
                ZepAbsence.builder()
                        .id(2)
                        .absenceReason(ZepAbsenceReason.builder().name("FA").build())
                        .startDate(LocalDate.of(2019, 1, 2))
                        .employeeId("2")
                        .build(),
                ZepAbsence.builder()
                        .id(2)
                        .absenceReason(ZepAbsenceReason.builder().name("UB").build())
                        .startDate(LocalDate.of(2019, 1, 2))
                        .employeeId("3")
                        .build()

        };
        List<ZepAbsence> zepAbsences = List.of(zepAbsencesArr);

        List<AbsenceTime> absences =  absenceMapper.mapList(zepAbsences);
        Iterator<ZepAbsence> zepAbsencesIterator = zepAbsences.iterator();
        absences.forEach(absence -> {
            ZepAbsence zepAbsence = zepAbsencesIterator.next();
            assertThat(zepAbsence.id()).isEqualTo(absence.getId());
            assertThat(zepAbsence.absenceReason().name()).isEqualTo(absence.getReason());
            assertThat(zepAbsence.employeeId()).isEqualTo(absence.getUserId());
            assertThat(zepAbsence.startDate()).isEqualTo(absence.getFromDate());
        });
    }
}
