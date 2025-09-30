package com.gepardec.mega.zep;

import com.gepardec.mega.db.entity.common.PaymentMethodType;
import com.gepardec.mega.db.entity.common.ProjectTaskType;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.MonthlyBillInfo;
import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectHoursSummary;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectTimeEntry;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.zep.impl.Rest;
import com.gepardec.mega.zep.rest.dto.ZepAbsence;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.dto.ZepBillingType;
import com.gepardec.mega.zep.rest.dto.ZepCategory;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import com.gepardec.mega.zep.rest.dto.ZepEmploymentPeriod;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectDetail;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.dto.ZepReceipt;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAttachment;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.rest.mapper.AbsenceMapper;
import com.gepardec.mega.zep.rest.mapper.ProjectEmployeesMapper;
import com.gepardec.mega.zep.rest.mapper.ProjectEntryMapper;
import com.gepardec.mega.zep.rest.mapper.ProjectMapper;
import com.gepardec.mega.zep.rest.service.AbsenceService;
import com.gepardec.mega.zep.rest.service.AttendanceService;
import com.gepardec.mega.zep.rest.service.EmployeeService;
import com.gepardec.mega.zep.rest.service.EmploymentPeriodService;
import com.gepardec.mega.zep.rest.service.ProjectService;
import com.gepardec.mega.zep.rest.service.ReceiptService;
import com.gepardec.mega.zep.rest.service.RegularWorkingTimesService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@QuarkusTest
class ZepRestServiceImplTest {

    @InjectMock
    RegularWorkingTimesService regularWorkingTimesService;

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

    @InjectMock
    ReceiptService receiptService;

    @InjectMock
    EmployeeService employeeService;

    @InjectMock
    EmploymentPeriodService employmentPeriodService;

    @InjectMock
    ProjectEntryMapper projectEntryMapper;

    @InjectMock
    AbsenceService absenceService;

    @InjectMock
    AbsenceMapper absenceMapper;

    @InjectMock
    UserContext userContext;

    @Test
    void getDoctorsVisitingTimeForMonthAndEmployee_whenUserHadDoctorsAppointments_thenReturnHours() {
        List<ZepAttendance> zepAttendancesForDoctorsAppointment = getZepAttendancesForDoctorsAppointment();

        when(attendanceService.getAttendanceForUserProjectAndMonth(anyString(), any(YearMonth.class), anyInt()))
                .thenReturn(zepAttendancesForDoctorsAppointment);

        double actual = zepRestService.getDoctorsVisitingTimeForMonthAndEmployee(createEmployee(), YearMonth.of(2024, 5));

        assertThat(actual).isEqualTo(zepAttendancesForDoctorsAppointment.stream()
                .map(ZepAttendance::duration)
                .reduce(Double::sum)
                .orElse(0d));
    }

    @Test
    void getDoctorsVisitingTimeForMonthAndEmployee_whenUserHadNoDoctorsAppointments_thenReturnZeroHours() {
        when(attendanceService.getAttendanceForUserProjectAndMonth(anyString(), any(YearMonth.class), anyInt()))
                .thenReturn(List.of());

        double actual = zepRestService.getDoctorsVisitingTimeForMonthAndEmployee(createEmployee(), YearMonth.of(2024, 5));

        assertThat(actual).isEqualTo(0.0);
    }

    @Test
    void getMonthlyBillInfoForEmployee_whenEmployeeHasBillInfoWithWarnings_thenReturnMonthlyBillInfo() {
        List<ZepReceipt> allReceipts = new ArrayList<>();
        ZepReceipt receipt = ZepReceipt.builder().employeeId("007-jbond").paymentMethodType(PaymentMethodType.PRIVATE.getPaymentMethodName()).id(1).build();
        ZepReceiptAttachment receiptAttachment = ZepReceiptAttachment.builder().fileContent("ABC").build();
        ZepReceipt receipt2 = ZepReceipt.builder().employeeId("007-jbond").paymentMethodType(PaymentMethodType.COMPANY.getPaymentMethodName()).id(2).build();
        allReceipts.add(receipt);
        allReceipts.add(receipt2);

        PersonioEmployee personioEmployee = PersonioEmployee.builder().hasCreditCard(true).build();
        Employee employee = Employee.builder().userId("007-jbond").build();

        when(receiptService.getAllReceiptsInRange(any(YearMonth.class)))
                .thenReturn(allReceipts);

        when(receiptService.getAttachmentByReceiptId(anyInt()))
                .thenReturn(Optional.of(receiptAttachment))
                .thenReturn(Optional.empty());

        MonthlyBillInfo result = zepRestService.getMonthlyBillInfoForEmployee(personioEmployee, employee, YearMonth.of(2024, 6));

        assertThat(result).isNotNull();
        assertThat(result.getSumBills()).isEqualTo(2);
        assertThat(result.getSumPrivateBills()).isOne();
        assertThat(result.getSumCompanyBills()).isOne();
        assertThat(result.getHasAttachmentWarnings()).isTrue();
        assertThat(result.getEmployeeHasCreditCard()).isTrue();
    }

    @Test
    void getMonthlyBillInfoForEmployee_whenEmployeeHasNoBillInfo_thenReturnEmptyMonthlyBillInfo() {
        List<ZepReceipt> allReceipts = new ArrayList<>();

        PersonioEmployee personioEmployee = PersonioEmployee.builder().hasCreditCard(true).build();
        Employee employee = Employee.builder().userId("007-jbond").build();

        when(receiptService.getAllReceiptsInRange(any(YearMonth.class)))
                .thenReturn(allReceipts);


        MonthlyBillInfo result = zepRestService.getMonthlyBillInfoForEmployee(personioEmployee, employee, YearMonth.of(2024, 6));

        assertThat(result).isNotNull();
        assertThat(result.getSumBills()).isZero();
        assertThat(result.getSumPrivateBills()).isZero();
        assertThat(result.getSumCompanyBills()).isZero();
        assertThat(result.getHasAttachmentWarnings()).isFalse();
        assertThat(result.getEmployeeHasCreditCard()).isTrue();
    }

    @Test
    void getAllProjectsForMonthAndEmployee_whenEmployeeHasProjects_thenReturnProjects() {
        Employee employee = Employee.builder().userId("007-jbond").build();
        ZepEmployee zepEmployee = ZepEmployee.builder().username("007-jbond").build();
        ZepProjectEmployee zepProjectEmployee = ZepProjectEmployee.builder().username("007-jbond").build();

        when(employeeService.getZepEmployeeByUsername("007-jbond"))
                .thenReturn(Optional.of(zepEmployee));

        when(projectService.getProjectsForMonthYear(any(YearMonth.class)))
                .thenReturn(
                        List.of(
                                ZepProject.builder().id(1).build()
                        )
                );

        when(projectService.getProjectEmployeesForId(anyInt()))
                .thenReturn(List.of(zepProjectEmployee));

        when(attendanceService.getAttendanceForUserProjectAndMonth(anyString(), any(YearMonth.class), anyInt()))
                .thenReturn(List.of(
                        ZepAttendance.builder().billable(true).duration(2.0).projectId(1).date(LocalDate.of(2024, 6, 1)).build(),
                        ZepAttendance.builder().billable(false).duration(2.0).projectId(1).date(LocalDate.of(2024, 6, 4)).build(),
                        ZepAttendance.builder().billable(true).duration(4.0).projectId(1).date(LocalDate.of(2024, 6, 12)).build()
                ));

        var zepProjectDetail = new ZepProjectDetail();
        zepProjectDetail.setProject(ZepProject.builder().id(1).name("XYZ").billingType(new ZepBillingType(1)).build());
        when(projectService.getProjectById(anyInt()))
                .thenReturn(Optional.of(zepProjectDetail));

        when(userContext.getUser()).thenReturn(User.builder().personioId(123).build());

        List<ProjectHoursSummary> result = zepRestService.getAllProjectsForMonthAndEmployee(employee, YearMonth.of(2024, 6));

        assertThat(result).isNotNull();
        assertThat(result.getFirst().getBillableHoursSum()).isEqualTo(6.0);
        assertThat(result.getFirst().getNonBillableHoursSum()).isEqualTo(2.0);
        assertThat(result.getFirst().getChargeability()).isEqualTo(75.0);
    }

    @Test
    void getAllProjectsForMonthAndEmployee_whenNoEmployeePresent_thenReturnListOf() {
        Employee employee = Employee.builder().userId("007-jbond").build();
        when(employeeService.getZepEmployeeByUsername(anyString()))
                .thenReturn(Optional.empty());

        List<ProjectHoursSummary> result = zepRestService.getAllProjectsForMonthAndEmployee(employee, YearMonth.of(2024, 6));

        assertThat(result).isNotNull()
                .isEmpty();
    }

    @Test
    void getEmployee_whenNoEmployeeWithId_thenReturnNull() {
        when(employeeService.getZepEmployeeByUsername(anyString()))
                .thenReturn(Optional.empty());

        Employee actual = zepRestService.getEmployee("007-jbond");

        assertThat(actual).isNull();
    }

    @Test
    void getEmployee_whenEmployeeWithId_thenReturnEmployee() {
        ZepRegularWorkingTimes regularWorkingTimes = ZepRegularWorkingTimes.builder()
                .monday(8.0)
                .tuesday(8.0)
                .wednesday(8.0)
                .thursday(8.0)
                .friday(6.5)
                .saturday(0.0)
                .sunday(0.0)
                .startDate(LocalDateTime.of(2024, 2, 10, 0, 0, 0))
                .build();

        when(employeeService.getZepEmployeeByUsername(anyString()))
                .thenReturn(Optional.of(
                        ZepEmployee.builder()
                                .username("007-jbond")
                                .firstname("James")
                                .lastname("Bond")
                                .build()
                ));

        when(employmentPeriodService.getZepEmploymentPeriodsByUsername(anyString()))
                .thenReturn(createZepEmploymentPeriodList());

        when(regularWorkingTimesService.getRegularWorkingTimesByUsername(anyString()))
                .thenReturn(List.of(regularWorkingTimes));

        when(userContext.getUser()).thenReturn(User.builder().personioId(123).build());


        Employee actual = zepRestService.getEmployee("007-jbond");

        assertThat(actual).isNotNull();
    }

    @Test
    void getEmployees_whenEmployeesPresent_thenReturnEmployees() {
        when(employeeService.getZepEmployees())
                .thenReturn(
                        createZepEmployeesList()
                );

        when(employmentPeriodService.getZepEmploymentPeriodsByUsername(anyString()))
                .thenReturn(createZepEmploymentPeriodList());

        when(userContext.getUser()).thenReturn(User.builder().personioId(123).build());

        List<Employee> actual = zepRestService.getEmployees();

        assertThat(actual)
                .isNotNull()
                .hasSize(2);
    }

    @Test
    void getProjectTimesForEmployeePerProject_whenProjectIsNotPresent_thenReturnListOf() {
        when(projectService.getProjectByName(anyString(), any(YearMonth.class)))
                .thenReturn(Optional.empty());

        List<ProjectTime> actual = zepRestService.getProjectTimesForEmployeePerProject("ABC", YearMonth.now());

        assertThat(actual)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void getProjectTimesForEmployeePerProject_whenProjectIsPresent_thenReturnProjectTimes() {
        when(projectService.getProjectByName(anyString(), any(YearMonth.class)))
                .thenReturn(Optional.of(
                        ZepProject.builder()
                                .id(1)
                                .name("ABC")
                                .build()
                ));

        when(projectService.getProjectEmployeesForId(anyInt()))
                .thenReturn(createZepProjectEmployees());

        when(attendanceService.getAttendanceForUserProjectAndMonth(anyString(), any(YearMonth.class), anyInt()))
                .thenReturn(createAttendancesList());

        List<ProjectTime> actual = zepRestService.getProjectTimesForEmployeePerProject("ABC", YearMonth.of(2024, 6));

        assertThat(actual)
                .isNotNull()
                .hasSize(6);
    }

    @ParameterizedTest
    @CsvSource({
            "007-jbond, 2024-06, true",
            "007-jbond, 2024-06, false"
    })
    void getProjectTimes_parametrized(String userId, YearMonth payrollMonth, boolean hasProjectTimes) {
        Employee employee = Employee.builder().userId(userId).build();

        if (hasProjectTimes) {
            List<ZepAttendance> zepAttendances = createAttendancesList();
            List<ProjectEntry> projectEntries = createProjectEntries();

            when(attendanceService.getAttendanceForUserAndMonth(userId, payrollMonth))
                    .thenReturn(zepAttendances);

            when(projectEntryMapper.mapList(zepAttendances))
                    .thenReturn(projectEntries);

            List<ProjectEntry> result = zepRestService.getProjectTimes(employee, payrollMonth);

            assertThat(result).isNotEmpty();
            assertThat(result).isEqualTo(projectEntries);
        } else {
            when(attendanceService.getAttendanceForUserAndMonth(userId, payrollMonth))
                    .thenReturn(Collections.emptyList());

            List<ProjectEntry> result = zepRestService.getProjectTimes(employee, payrollMonth);
            assertThat(result).isEmpty();
        }
    }

    @Test
    void getProjectByName_whenProjectIsPresent_thenReturnProject() {
        when(projectService.getProjectByName(anyString(), any(YearMonth.class)))
                .thenReturn(Optional.of(
                        ZepProject.builder()
                                .name("ABC")
                                .id(1)
                                .startDate(LocalDateTime.of(2024, 6, 12, 10, 0))
                                .build()
                ));

        when(projectMapper.map(any(ZepProject.class)))
                .thenReturn(Project.builder().projectId("ABC").startDate(LocalDate.of(2024, 6, 12)));

        Optional<Project> actual = zepRestService.getProjectByName("ABC", YearMonth.of(2024, 6));

        assertThat(actual).isPresent();
        assertThat(actual.get().getProjectId()).isEqualTo("ABC");
    }

    @Test
    void getAbsenceForEmployee_whenAbsences_thenReturnListOfAbsences() {
        List<AbsenceTime> absenceTimes = createAbsencesList();
        Employee employee = Employee.builder().userId("007-jbond").build();

        try (MockedStatic<DateUtils> dateUtilsMockedStatic = mockStatic(DateUtils.class)) {
            dateUtilsMockedStatic.when(() -> DateUtils.getFirstDayOfMonth(anyInt(), anyInt()))
                    .thenReturn(LocalDate.of(2024, 5, 1));
            dateUtilsMockedStatic.when(() -> DateUtils.getLastDayOfMonth(anyInt(), anyInt()))
                    .thenReturn(LocalDate.of(2024, 5, 30));

            when(absenceService.getZepAbsencesByEmployeeNameForDateRange(anyString(), any(YearMonth.class)))
                    .thenReturn(createZepAbsencesList());

            when(absenceMapper.mapList(any()))
                    .thenReturn(absenceTimes);

            List<AbsenceTime> actual = zepRestService.getAbsenceForEmployee(employee, YearMonth.of(2024, 5));

            assertThat(actual)
                    .isNotNull()
                    .hasSize(2);
        }
    }

    private List<AbsenceTime> createAbsencesList() {
        List<AbsenceTime> absenceTimes = new ArrayList<>();
        absenceTimes.add(
                AbsenceTime.builder()
                        .fromDate(LocalDate.of(2024, 5, 6))
                        .toDate(LocalDate.of(2024, 5, 8))
                        .build()
        );

        absenceTimes.add(
                AbsenceTime.builder()
                        .fromDate(LocalDate.of(2024, 5, 6))
                        .toDate(LocalDate.of(2024, 5, 15))
                        .build()
        );

        return absenceTimes;
    }

    private List<ZepAbsence> createZepAbsencesList() {
        List<ZepAbsence> zepAbsences = new ArrayList<>();
        zepAbsences.add(
                ZepAbsence.builder()
                        .id(1)
                        .startDate(LocalDate.of(2024, 5, 6))
                        .endDate(LocalDate.of(2024, 5, 8))
                        .build()
        );

        zepAbsences.add(
                ZepAbsence.builder()
                        .id(2)
                        .startDate(LocalDate.of(2024, 5, 15))
                        .endDate(LocalDate.of(2024, 5, 18))
                        .build()
        );

        return zepAbsences;
    }

    private List<ProjectEntry> createProjectEntries() {
        List<ProjectEntry> projectEntries = new ArrayList<>();

        projectEntries.add(
                ProjectTimeEntry.builder()
                        .fromTime(LocalDateTime.of(2024, 6, 3, 13, 20))
                        .toTime(LocalDateTime.of(2024, 6, 3, 16, 20))
                        .build()
        );

        projectEntries.add(
                ProjectTimeEntry.builder()
                        .fromTime(LocalDateTime.of(2024, 6, 4, 13, 20))
                        .toTime(LocalDateTime.of(2024, 6, 4, 16, 20))
                        .build()
        );

        projectEntries.add(
                ProjectTimeEntry.builder()
                        .fromTime(LocalDateTime.of(2024, 6, 5, 13, 20))
                        .toTime(LocalDateTime.of(2024, 6, 5, 16, 20))
                        .build()
        );

        return projectEntries;
    }

    private List<ZepAttendance> createAttendancesList() {
        List<ZepAttendance> attendanceList = new ArrayList<>();
        attendanceList.add(
                ZepAttendance.builder()
                        .id(1)
                        .duration(3.0)
                        .projectId(1)
                        .date(LocalDate.of(2024, 6, 3))
                        .from(LocalTime.of(13, 20))
                        .to(LocalTime.of(16, 30))
                        .build()
        );

        attendanceList.add(
                ZepAttendance.builder()
                        .id(2)
                        .duration(5.0)
                        .projectId(1)
                        .date(LocalDate.of(2024, 6, 4))
                        .from(LocalTime.of(13, 20))
                        .to(LocalTime.of(16, 30))
                        .build()
        );

        attendanceList.add(
                ZepAttendance.builder()
                        .id(3)
                        .duration(4.0)
                        .projectId(1)
                        .date(LocalDate.of(2024, 6, 5))
                        .from(LocalTime.of(13, 20))
                        .to(LocalTime.of(16, 30))
                        .build()
        );

        return attendanceList;
    }

    private List<ZepProjectEmployee> createZepProjectEmployees() {
        List<ZepProjectEmployee> projectEmployees = new ArrayList<>();
        projectEmployees.add(
                ZepProjectEmployee.builder()
                        .username("008-mmustermann")
                        .build()
        );

        projectEmployees.add(
                ZepProjectEmployee.builder()
                        .username("009-mmusterfrau")
                        .build()
        );

        return projectEmployees;
    }

    private List<ZepEmployee> createZepEmployeesList() {
        List<ZepEmployee> employees = new ArrayList<>();
        employees.add(
                ZepEmployee.builder()
                        .username("007-jbond")
                        .build()
        );

        employees.add(
                ZepEmployee.builder()
                        .username("008-mmustermann")
                        .build()
        );

        return employees;
    }

    private List<ZepEmploymentPeriod> createZepEmploymentPeriodList() {
        List<ZepEmploymentPeriod> employmentPeriods = new ArrayList<>();
        employmentPeriods.add(
                ZepEmploymentPeriod.builder()
                        .startDate(LocalDateTime.of(2024, 2, 10, 0, 0, 0))
                        .build()
        );
        return employmentPeriods;
    }

    @Nested
    class GetProjectsForMonthYear {

        private final ZepProject zepProject = ZepProject.builder().id(1).billingType(new ZepBillingType(1)).build();
        private final YearMonth payrollMonth = YearMonth.now();

        @BeforeEach
        void setUp() {
            when(projectService.getProjectsForMonthYear(payrollMonth))
                    .thenReturn(List.of(zepProject));
            when(projectService.getProjectById(1))
                    .thenReturn(Optional.of(createZepProjectDetail(zepProject)));
            when(projectEmployeesMapper.map(any())).thenReturn(new MultivaluedHashMap<>());
        }

        @Test
        void thenShouldMapCategoriesToProject() {
            assertThat(zepRestService.getProjectsForMonthYear(payrollMonth))
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
