package com.gepardec.mega.zep;

import com.gepardec.mega.db.entity.common.ProjectTaskType;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.zep.impl.Rest;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.dto.ZepBillingType;
import com.gepardec.mega.zep.rest.dto.ZepCategory;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectDetail;
import com.gepardec.mega.zep.rest.mapper.ProjectEmployeesMapper;
import com.gepardec.mega.zep.rest.mapper.ProjectMapper;
import com.gepardec.mega.zep.rest.service.AttendanceService;
import com.gepardec.mega.zep.rest.service.ProjectService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
class ZepRestServiceImplTest {

    @Inject
    @Rest
    ZepService zepRestService;

    @InjectMock
    AttendanceService attendanceService;

    @InjectSpy
    ProjectMapper projectMapper;

    @InjectMock
    ProjectEmployeesMapper projectEmployeesMapper;

    @InjectMock
    ProjectService projectService;

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

    @Nested
    class GetProjectsForMonthYear {

        private final ZepProject zepProject = ZepProject.builder().id(1).billingType(new ZepBillingType(1)).build();
        private final LocalDate currentMonthYear = DateUtils.getFirstDayOfCurrentMonth();

        @BeforeEach
        void setUp() {
            when(projectService.getProjectsForMonthYear(currentMonthYear))
                    .thenReturn(List.of(zepProject));
            when(projectService.getProjectById(1))
                    .thenReturn(Optional.of(createZepProjectDetail(zepProject)));
            when(projectEmployeesMapper.map(any())).thenReturn(new MultivaluedHashMap<>());
        }

        @Test
        void thenShouldMapCategoriesToProject() {
            assertThat(zepRestService.getProjectsForMonthYear(currentMonthYear))
                    .hasSize(1)
                    .first()
                    .extracting(Project::getCategories)
                    .isEqualTo(List.of("INT"));
        }

        private ZepProjectDetail createZepProjectDetail(ZepProject zepProject) {
            var zepProjectDetail = new ZepProjectDetail();
            zepProjectDetail.setProject(zepProject);
            zepProjectDetail.setCategories(List.of(new ZepCategory("INT", Collections.emptyMap())));

            return zepProjectDetail;
        }
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
