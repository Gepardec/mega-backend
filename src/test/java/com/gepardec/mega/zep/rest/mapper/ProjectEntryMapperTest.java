package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.monthlyreport.JourneyDirection;
import com.gepardec.mega.domain.model.monthlyreport.JourneyTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.Task;
import com.gepardec.mega.domain.model.monthlyreport.Vehicle;
import com.gepardec.mega.domain.model.monthlyreport.WorkingLocation;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.dto.ZepAttendanceDirectionOfTravel;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;

@QuarkusTest
class ProjectEntryMapperTest {

    @Inject
    ProjectEntryMapper projectEntryMapper;

    @InjectMock
    Logger logger;

    @Test
    void mapZepAttendanceToProjectEntry() {
        ZepAttendance.Builder zepAttendance = generateZepAttendanceBuilder();
        ProjectEntry mappedProjectEntry = projectEntryMapper.map(zepAttendance.build());
        ProjectEntry expectedProjectEntry = generateProjectTimeEntry();

        assertThat(mappedProjectEntry).usingRecursiveComparison().isEqualTo(expectedProjectEntry);
    }

    @Test
    void mapZepAttendanceJourneyTimeEntry() {
        ZepAttendance.Builder zepAttendance = generateZepAttendanceBuilder();
        ZepAttendanceDirectionOfTravel zepAttendanceDirectionOfTravel = ZepAttendanceDirectionOfTravel.builder()
                .id("0")
                .name("return")
                .build();
        zepAttendance.activity("reisen");
        zepAttendance.workLocation("A");
        zepAttendance.directionOfTravel(zepAttendanceDirectionOfTravel);
        zepAttendance.vehicle("Auto (PKW passiv)");

        ProjectEntry mappedProjectEntry = projectEntryMapper.map(zepAttendance.build());
        ProjectEntry expectedProjectEntry = generateJourneyTimeEntry();

        assertThat(mappedProjectEntry).usingRecursiveComparison().isEqualTo(expectedProjectEntry);
    }

    @Test
    void mapZepAttendanceToProjectEntryWithNullValues_JourneyTimeEntry() {
        ZepAttendance.Builder zepAttendance = generateZepAttendanceBuilder();
        zepAttendance.activity("reisen");
        zepAttendance.workLocation(null);
        zepAttendance.vehicle(null);

        JourneyTimeEntry mappedProjectEntry = (JourneyTimeEntry) projectEntryMapper.map(zepAttendance.build());
        JourneyTimeEntry expectedProjectEntry = generateJourneyTimeWithOtherValuesEntry();

        assertThat(mappedProjectEntry).usingRecursiveComparison().isEqualTo(expectedProjectEntry);
    }

    @Test
    void map_whenZepAttendanceIsNull_thenReturnNullAndLogMessage() {
        assertThat(projectEntryMapper.map(null)).isNull();
        verify(logger).info("ZEP REST implementation -- While trying to map zepAttendance to ProjectEntry, zepAttendance was null");
    }

    @Test
    void mapList_whenZepAttendanceListIsNull_thenReturnNull() {
        assertThat(projectEntryMapper.mapList(null)).isEmpty();
    }

    @Test
    void mapZepAttendanceToProjectEntryWithNullValues_IllegalArgumentException_toTask() {
        ZepAttendance.Builder zepAttendance = generateZepAttendanceBuilder();
        zepAttendance.activity("nasebohren");

        ThrowableAssert.ThrowingCallable throwingCallable = () -> projectEntryMapper.map(zepAttendance.build());
        assertThatExceptionOfType(ZepServiceException.class).isThrownBy(throwingCallable);
    }

    @Test
    void mapListToProjektEntry_thenReturnList() {
        List<ZepAttendance> zepList = new ArrayList<ZepAttendance>();
        zepList.add(generateZepAttendanceBuilder().build());
        zepList.add(generateZepAttendanceBuilder().build());
        List<ProjectEntry> mappedProjectEntryList = projectEntryMapper.mapList(zepList);

        assertThat(mappedProjectEntryList).hasSize(2);
        assertThat(mappedProjectEntryList.getFirst()).isNotNull();
        assertThat(mappedProjectEntryList.get(1)).isNotNull();
    }

    private ZepAttendance.Builder generateZepAttendanceBuilder() {
        return ZepAttendance.builder()
                .id(1)
                .date(LocalDate.of(2019, 1, 2))
                .from(LocalTime.of(9, 15))
                .to(LocalTime.of(10, 15))
                .employeeId("001")
                .projectId(1)
                .projectTaskId(30)
                .duration(1.0)
                .billable(true)
                .workLocation("A")
                .workLocationIsProjectRelevant(false)
                .activity("bearbeiten")
                .vehicle(null)
                .directionOfTravel(null);
    }

    private ProjectTimeEntry generateProjectTimeEntry() {
        return ProjectTimeEntry.builder()
                .fromTime(LocalDateTime.of(2019, 1, 2, 9, 15))
                .toTime(LocalDateTime.of(2019, 1, 2, 10, 15))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.A)
                .workLocationIsProjectRelevant(false)
                .process("30")
                .build();
    }

    private JourneyTimeEntry generateJourneyTimeEntry() {
        return JourneyTimeEntry.builder()
                .fromTime(LocalDateTime.of(2019, 1, 2, 9, 15))
                .toTime(LocalDateTime.of(2019, 1, 2, 10, 15))
                .task(Task.REISEN)
                .journeyDirection(JourneyDirection.TO)
                .workingLocation(WorkingLocation.A)
                .workLocationIsProjectRelevant(false)
                .vehicle(Vehicle.CAR_INACTIVE)
                .build();
    }

    private JourneyTimeEntry generateJourneyTimeWithOtherValuesEntry() {
        return JourneyTimeEntry.builder()
                .fromTime(LocalDateTime.of(2019, 1, 2, 9, 15))
                .toTime(LocalDateTime.of(2019, 1, 2, 10, 15))
                .task(Task.REISEN)
                .journeyDirection(JourneyDirection.TO)
                .workingLocation(WorkingLocation.A)
                .vehicle(Vehicle.OTHER_INACTIVE)
                .workingLocation(WorkingLocation.MAIN)
                .workLocationIsProjectRelevant(false)
                .build();
    }
}
