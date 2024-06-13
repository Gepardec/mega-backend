package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.zep.rest.dto.ZepAbsence;
import com.gepardec.mega.zep.rest.dto.ZepAbsenceReason;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@QuarkusTest
class AbsenceMapperTest {

    @Inject
    AbsenceMapper absenceMapper;

    @InjectMock
    Logger logger;

    @Test
    void mapZepAbsenceToAbsenceTime() {
        ZepAbsenceReason zepAbsenceReason = ZepAbsenceReason.builder().name("KR").build();
        ZepAbsence zepAbsence = ZepAbsence.builder()
                .id(1)
                .employeeId("001")
                .startDate(LocalDate.of(2019, 1, 2))
                .endDate(LocalDate.of(2019, 1, 5))
                .absenceReason(zepAbsenceReason)
                .approved(true)
                .build();

        AbsenceTime absence = absenceMapper.map(zepAbsence);

        assertThat(absence.userId()).isEqualTo(zepAbsence.employeeId());
        assertThat(absence.fromDate()).isEqualTo(zepAbsence.startDate());
        assertThat(absence.toDate()).isEqualTo(zepAbsence.endDate());
        assertThat(absence.reason()).isEqualTo(zepAbsence.absenceReason().name());
        assertThat(absence.accepted()).isEqualTo(zepAbsence.approved());
    }

    @Test
    void mapZepAbsenceToAbsenceTime_whenReasonIsNull_thenReturnAbsenceTimeWithoutReason() {
        ZepAbsence zepAbsence = ZepAbsence.builder()
                .id(1)
                .employeeId("001")
                .startDate(LocalDate.of(2019, 1, 2))
                .endDate(LocalDate.of(2019, 1, 5))
                .absenceReason(null)
                .approved(true)
                .build();

        AbsenceTime absence = absenceMapper.map(zepAbsence);

        assertThat(absence.userId()).isEqualTo(zepAbsence.employeeId());
        assertThat(absence.fromDate()).isEqualTo(zepAbsence.startDate());
        assertThat(absence.toDate()).isEqualTo(zepAbsence.endDate());
        assertThat(absence.reason()).isEqualTo("");
        assertThat(absence.accepted()).isEqualTo(zepAbsence.approved());
    }

    @Test
    void mapZepAbsenceToAbsenceTime_whenZepAbsenceIsNull_thenReturnNullAndLogMessage() {
        ZepAbsence zepAbsence = null;

        AbsenceTime absence = absenceMapper.map(zepAbsence);

        assertThat(absence).isNull();
        verify(logger).info("ZEP REST implementation -- While trying to map ZepAbsence to AbsenceTime, ZepAbsence was null");
    }

    @Test
    void mapZepAbsencesToAbsenceTimes() {
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
            assertThat(zepAbsence.absenceReason().name()).isEqualTo(absence.reason());
            assertThat(zepAbsence.employeeId()).isEqualTo(absence.userId());
            assertThat(zepAbsence.startDate()).isEqualTo(absence.fromDate());
        });
    }
}
