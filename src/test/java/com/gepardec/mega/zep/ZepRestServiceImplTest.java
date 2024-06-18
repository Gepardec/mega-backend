package com.gepardec.mega.zep;

import com.gepardec.mega.db.entity.common.PaymentMethodType;
import com.gepardec.mega.db.entity.common.ProjectTaskType;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.MonthlyBillInfo;
import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.domain.model.ProjectHoursSummary;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.service.api.DateHelperService;
import com.gepardec.mega.zep.impl.Rest;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.dto.ZepReceipt;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAttachment;
import com.gepardec.mega.zep.rest.service.AttendanceService;
import com.gepardec.mega.zep.rest.service.EmployeeService;
import com.gepardec.mega.zep.rest.service.ProjectService;
import com.gepardec.mega.zep.rest.service.ReceiptService;
import com.gepardec.mega.zep.rest.service.RegularWorkingTimesService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
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
    @Inject
    RegularWorkingTimesService regularWorkingTimesService;

    @Inject @Rest
    ZepService zepRestService;

    @InjectMock
    AttendanceService attendanceService;

    @InjectMock
    DateHelperService dateHelperService;

    @InjectMock
    ReceiptService receiptService;

    @InjectMock
    EmployeeService employeeService;

    @InjectMock
    ProjectService projectService;


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
        String fromDate = LocalDate.of(2024, 5, 1).toString();
        String toDate = LocalDate.of(2024, 5, 30).toString();

        when(attendanceService.getAttendanceForUserProjectAndMonth(anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(zepAttendancesForDoctorsAppointment);

        when(dateHelperService.getCorrectDateForRequest(any(Employee.class), any(YearMonth.class)))
                .thenReturn(Pair.of(fromDate, toDate));

        double actual = zepRestService.getDoctorsVisitingTimeForMonthAndEmployee(createEmployee(), YearMonth.of(2024, 5));

        assertThat(actual).isEqualTo(zepAttendancesForDoctorsAppointment.stream()
                                                                        .map(ZepAttendance::duration)
                                                                        .reduce(Double::sum)
                                                                        .get());
    }

    @Test
    void getDoctorsVisitingTimeForMonthAndEmployee_whenUserHadNoDoctorsAppointments_thenReturnZeroHours() {
        String fromDate = LocalDate.of(2024, 5, 1).toString();

        when(attendanceService.getAttendanceForUserProjectAndMonth(anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(List.of());

        when(dateHelperService.getCorrectDateForRequest(any(Employee.class), any(YearMonth.class)))
                .thenReturn(Pair.of(fromDate, null));

        double actual = zepRestService.getDoctorsVisitingTimeForMonthAndEmployee(createEmployee(), YearMonth.of(2024, 5));

        assertThat(actual).isEqualTo(0.0);
    }

    @Test
    void getMonthlyBillInfoForEmployee_whenEmployeeHasBillInfoWithWarnings_thenReturnMonthlyBillInfo() {
        LocalDate fromDate = LocalDate.of(2024,6,13);
        String toDateString = LocalDate.of(2024,6,30).toString();
        Pair<String, String> datePair = Pair.of(fromDate.toString(), toDateString);

        List<ZepReceipt> allReceipts = new ArrayList<>();
        ZepReceipt receipt= ZepReceipt.builder().employeeId("007-jbond").paymentMethodType(PaymentMethodType.PRIVATE.getPaymentMethodName()).id(1).build();
        ZepReceiptAttachment receiptAttachment= ZepReceiptAttachment.builder().fileContent("ABC").build();
        ZepReceipt receipt2 = ZepReceipt.builder().employeeId("007-jbond").paymentMethodType(PaymentMethodType.COMPANY.getPaymentMethodName()).id(2).build();
        ZepReceiptAttachment receipt2Attachment= ZepReceiptAttachment.builder().fileContent("DEF").build();
        allReceipts.add(receipt);
        allReceipts.add(receipt2);

        PersonioEmployee personioEmployee = PersonioEmployee.builder().hasCreditCard(true).build();
        Employee employee = Employee.builder().userId("007-jbond").build();

        when(dateHelperService.getCorrectDateForRequest(any(Employee.class), any(YearMonth.class)))
                .thenReturn(datePair);

        when(receiptService.getAllReceiptsForYearMonth(any(Employee.class), anyString(), anyString()))
                .thenReturn(allReceipts);

        when(receiptService.getAttachmentByReceiptId(anyInt()))
                .thenReturn(Optional.of(receiptAttachment))
                .thenReturn(Optional.empty());

        MonthlyBillInfo result = zepRestService.getMonthlyBillInfoForEmployee(personioEmployee, employee, YearMonth.of(2024,6));

        assertThat(result).isNotNull();
        assertThat(result.getSumBills()).isEqualTo(2);
        assertThat(result.getSumPrivateBills()).isOne();
        assertThat(result.getSumCompanyBills()).isOne();
        assertThat(result.getHasAttachmentWarnings()).isTrue();
        assertThat(result.getEmployeeHasCreditCard()).isTrue();
    }

    @Test
    void getMonthlyBillInfoForEmployee_whenEmployeeHasNoBillInfo_thenReturnEmptyMonthlyBillInfo() {
        LocalDate fromDate = LocalDate.of(2024,6,13);
        String toDateString = LocalDate.of(2024,6,30).toString();
        Pair<String, String> datePair = Pair.of(fromDate.toString(), toDateString);

        List<ZepReceipt> allReceipts = new ArrayList<>();

        PersonioEmployee personioEmployee = PersonioEmployee.builder().hasCreditCard(true).build();
        Employee employee = Employee.builder().userId("007-jbond").build();

        when(dateHelperService.getCorrectDateForRequest(any(Employee.class), any(YearMonth.class)))
                .thenReturn(datePair);

        when(receiptService.getAllReceiptsForYearMonth(any(Employee.class), anyString(), anyString()))
                .thenReturn(allReceipts);


        MonthlyBillInfo result = zepRestService.getMonthlyBillInfoForEmployee(personioEmployee, employee, YearMonth.of(2024,6));

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
        LocalDate fromDate = LocalDate.of(2024,6,1);
        String toDateString = LocalDate.of(2024,6,30).toString();
        Pair<String, String> datePair = Pair.of(fromDate.toString(), toDateString);


        when(employeeService.getZepEmployeeByUsername("007-jbond"))
                .thenReturn(Optional.of(zepEmployee));

        when(dateHelperService.getCorrectDateForRequest(any(Employee.class), any(YearMonth.class)))
                .thenReturn(datePair);

        when(projectService.getProjectsForMonthYear(any(LocalDate.class)))
                .thenReturn(
                        List.of(
                                ZepProject.builder().id(1).build()
                        )
                );

        when(projectService.getProjectEmployeesForId(anyInt()))
                .thenReturn(List.of(zepProjectEmployee));

        when(attendanceService.getAttendanceForUserProjectAndMonth(anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(List.of(
                        ZepAttendance.builder().billable(true).duration(2.0).projectId(1).date(LocalDate.of(2024,6,1)).build(),
                        ZepAttendance.builder().billable(false).duration(2.0).projectId(1).date(LocalDate.of(2024,6,4)).build(),
                        ZepAttendance.builder().billable(true).duration(4.0).projectId(1).date(LocalDate.of(2024,6,12)).build()
                ));

        when(projectService.getProjectById(anyInt()))
                .thenReturn(Optional.of(ZepProject.builder().id(1).name("XYZ").build()));

        List<ProjectHoursSummary> result = zepRestService.getAllProjectsForMonthAndEmployee(employee, YearMonth.of(2024,6));

        assertThat(result).isNotNull();
        assertThat(result.get(0).getBillableHoursSum()).isEqualTo(6.0);
        assertThat(result.get(0).getNonBillableHoursSum()).isEqualTo(2.0);
        assertThat(result.get(0).getChargeability()).isEqualTo(75.0);
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
