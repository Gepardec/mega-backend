package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.monthlyreport.*;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.entity.ZepAttendance;
import com.gepardec.mega.zep.rest.entity.ZepBillable;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
public class ProjectEntryMapperTest {

    @Inject
    ProjectEntryMapper projectEntryMapper;

    @Test
    public void mapZepAttendanceToProjectEntry() {
        ZepAttendance.Builder zepAttendance = generateZepAttendanceBuilder();
        ProjectEntry mappedProjectEntry = projectEntryMapper.map(zepAttendance.build());
        ProjectEntry expectedProjectEntry = generateProjectTimeEntry();

        assertThat(mappedProjectEntry).usingRecursiveComparison().isEqualTo(expectedProjectEntry);
    }

    @Test
    public void mapZepAttendanceJourneyTimeEntry() {
        ZepAttendance.Builder zepAttendance = generateZepAttendanceBuilder();
        zepAttendance.activity("reisen");
        zepAttendance.workLocation("A");
        zepAttendance.directionOfTravel("0");
        zepAttendance.vehicle("Auto (PKW passiv)");

        ProjectEntry mappedProjectEntry = projectEntryMapper.map(zepAttendance.build());
        ProjectEntry expectedProjectEntry = generateJourneyTimeEntry();

        assertThat(mappedProjectEntry).usingRecursiveComparison().isEqualTo(expectedProjectEntry);
    }

    @Test
    public void mapZepAttendanceToProjectEntryWithNullValues_JourneyTimeEntry() {
        ZepAttendance.Builder zepAttendance = generateZepAttendanceBuilder();
        zepAttendance.activity("reisen");
        zepAttendance.workLocation(null);
        zepAttendance.vehicle(null);

        JourneyTimeEntry mappedProjectEntry = (JourneyTimeEntry) projectEntryMapper.map(zepAttendance.build());
        JourneyTimeEntry expectedProjectEntry = generateJourneyTimeWithOtherValuesEntry();

        assertThat(mappedProjectEntry).usingRecursiveComparison().isEqualTo(expectedProjectEntry);
    }

    @Test
    public void mapZepAttendanceToProjectEntryWithNullValues_IllegalArgumentException_toTask() {
        ZepAttendance.Builder zepAttendance = generateZepAttendanceBuilder();
        zepAttendance.activity("nasebohren");

        assertThrows(ZepServiceException.class, () -> {
            ProjectEntry mappedProjectEntry = projectEntryMapper.map(zepAttendance.build());
        });
    }

    @Test
    public void mapListToProjektEntry_thenReturnList(){
        List<ZepAttendance> Zeplist = new ArrayList<ZepAttendance>();
        Zeplist.add(generateZepAttendanceBuilder().build());
        Zeplist.add(generateZepAttendanceBuilder().build());
        List<ProjectEntry> mappedProjectEntryList = projectEntryMapper.mapList(Zeplist);

        assertThat(mappedProjectEntryList.size()).isEqualTo(2);
        assertThat(mappedProjectEntryList.get(0)).isNotNull();
        assertThat(mappedProjectEntryList.get(1)).isNotNull();
    }


    private ZepAttendance.Builder generateZepAttendanceBuilder(){
        return ZepAttendance.builder()
                .id(1)
                .date(LocalDate.of(2019, 1, 2))
                .from(LocalTime.of(9, 15))
                .to(LocalTime.of(10,15))
                .employeeId("001")
                .projectId(1)
                .projectTaskId(30)
                .duration(1.0)
                .billable(ZepBillable.builder().id(2).name("Billable").build())
                .workLocation("A")
                .workLocationIsProjectRelevant(false)
                .note("SomeNote")
                .activity("bearbeiten")
                .start(null)
                .destination(null)
                .vehicle(null)
                .isPrivate(0)
                .passengers(null)
                .km(null)
                .directionOfTravel(null)
                .ticketId(null)
                .subtaskId(null)
                .invoiceItemId(null)
                .created(LocalDateTime.of(2019, 1, 12, 16, 32, 29))
                .modified(LocalDateTime.of(2019, 1, 12, 18, 32, 29));
    }

    private ProjectTimeEntry generateProjectTimeEntry(){
        return ProjectTimeEntry.builder()
                .fromTime(LocalDateTime.of(2019, 1, 2, 9, 15))
                .toTime(LocalDateTime.of(2019, 1, 2, 10, 15))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.A)
                .process("30")
                .build();
    }

    private JourneyTimeEntry generateJourneyTimeEntry(){
        return JourneyTimeEntry.builder()
                .fromTime(LocalDateTime.of(2019, 1, 2, 9, 15))
                .toTime(LocalDateTime.of(2019, 1, 2, 10, 15))
                .task(Task.REISEN)
                .journeyDirection(JourneyDirection.TO)
                .workingLocation(WorkingLocation.A)
                .vehicle(Vehicle.CAR_INACTIVE)
                .build();
    }
    private JourneyTimeEntry generateJourneyTimeWithOtherValuesEntry(){
        return JourneyTimeEntry.builder()
                .fromTime(LocalDateTime.of(2019, 1, 2, 9, 15))
                .toTime(LocalDateTime.of(2019, 1, 2, 10, 15))
                .task(Task.REISEN)
                .journeyDirection(JourneyDirection.TO)
                .workingLocation(WorkingLocation.A)
                .vehicle(Vehicle.OTHER_INACTIVE)
                .workingLocation(WorkingLocation.OTHER)
                .build();
    }






}
