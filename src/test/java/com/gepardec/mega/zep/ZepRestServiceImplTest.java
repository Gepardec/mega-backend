package com.gepardec.mega.zep;

import com.gepardec.mega.db.entity.common.ProjectTaskType;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.zep.impl.Rest;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.service.AttendanceService;
import com.gepardec.mega.zep.rest.service.RegularWorkingTimesService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@QuarkusTest
class ZepRestServiceImplTest {
    @Inject
    RegularWorkingTimesService regularWorkingTimesService;

    @Inject @Rest
    ZepService zepRestService;

    @InjectMock
    AttendanceService attendanceService;

//    @Inject
//    ZepEmployeeRestClient zepEmployeeRestClient;

    @Test
    @Disabled("To be deleted")
    public void ſ() {

        regularWorkingTimesService.getRegularWorkingTimesByUsername("001-hwirnsberger");
    }

    @Test
    @Disabled("Local test")
    void integrationTest_getProjectTimesForEmployeePerProject(){
        List<ProjectTime> projectTimes = zepRestService.getProjectTimesForEmployeePerProject("ITSV-VAEB-2018", LocalDate.of(2018, 12, 12));
        for (ProjectTime projectTime : projectTimes) {
            System.out.println(projectTime.getUserId() + ": " + projectTime.getDuration() + " " + projectTime.getBillable());
        }
        System.out.println(projectTimes.size());
    }

    @Test
    void getDoctorsVisitingTimeForMonthAndEmployee_whenUserHadDoctorsAppointments_thenReturnHours() {
        List<ZepAttendance> zepAttendancesForDoctorsAppointment = getZepAttendancesForDoctorsAppointment();
        when(attendanceService.getAttendanceForUserProjectAndMonth(anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(zepAttendancesForDoctorsAppointment);

        double actual = zepRestService.getDoctorsVisitingTimeForMonthAndEmployee(createEmployee(), YearMonth.of(2024, 5));

        assertThat(actual).isEqualTo(zepAttendancesForDoctorsAppointment.stream()
                                                                        .map(ZepAttendance::duration)
                                                                        .reduce(Double::sum)
                                                                        .get());
    }

    @Test
    void getDoctorsVisitingTimeForMonthAndEmployee_whenUserHadNoDoctorsAppointments_thenReturnZeroHours() {
        when(attendanceService.getAttendanceForUserProjectAndMonth(anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(List.of());

        double actual = zepRestService.getDoctorsVisitingTimeForMonthAndEmployee(createEmployee(), YearMonth.of(2024, 5));

        assertThat(actual).isEqualTo(0.0);
    }

    private ZepAttendance createZepAttendance(double duration) {
        return ZepAttendance.builder()
                            .id(1)
                            .duration(duration)
                            .projectId(ProjectTaskType.PROJECT_INTERNAL.getId())
                            .projectTaskId(ProjectTaskType.TASK_DOCTOR_VISIT.getId())
                            .build();
    }
    private List<ZepAttendance> getZepAttendancesForDoctorsAppointment() {
        return List.of(
            createZepAttendance(1.0),
            createZepAttendance(2.0),
            createZepAttendance(0.5),
            createZepAttendance(0.25)
        );
    }

    private Employee createEmployee() {
        return Employee.builder()
                       .userId("testUser2")
                       .build();
    }
}
