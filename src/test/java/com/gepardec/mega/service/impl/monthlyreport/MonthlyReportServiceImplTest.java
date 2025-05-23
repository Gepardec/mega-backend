package com.gepardec.mega.service.impl.monthlyreport;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.EmploymentPeriod;
import com.gepardec.mega.domain.model.EmploymentPeriods;
import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.domain.model.RegularWorkingTime;
import com.gepardec.mega.domain.model.RegularWorkingTimes;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.domain.model.monthlyreport.AbsenteeType;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.Task;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarningType;
import com.gepardec.mega.domain.model.monthlyreport.WorkingLocation;
import com.gepardec.mega.personio.employees.PersonioEmployeesService;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.helper.WarningCalculatorsManager;
import com.gepardec.mega.service.impl.MonthlyReportServiceImpl;
import com.gepardec.mega.zep.ZepService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
@OidcSecurity(claims = {
        @Claim(key = "email", value = "test@gepardec.com")
})
class MonthlyReportServiceImplTest {

    @InjectMock
    ZepService zepService;

    @InjectMock
    WarningCalculatorsManager warningCalculatorsManager;

    @InjectMock
    PersonioEmployeesService personioEmployeesService;

    @Inject
    MonthlyReportServiceImpl monthlyReportService;

    @InjectMock
    EmployeeService employeeService;

    @InjectMock
    UserContext userContext;

    MockedStatic<UserContext> mockStatic;

    @BeforeEach
    void init() {
        mockStatic = mockStatic(UserContext.class);

        User user = createUser();
        when(userContext.getUser()).thenReturn(user);

        Employee employee1 = createEmployeeForUser(user);
        when(employeeService.getEmployee(anyString())).thenReturn(employee1);

        PersonioEmployee personioEmployee = PersonioEmployee.builder()
                .guildLead("guildLead")
                .internalProjectLead("internalProjectLead")
                .vacationDayBalance(0d)
                .build();

        when(personioEmployeesService.getPersonioEmployeeByEmail(any()))
                .thenReturn(Optional.of(personioEmployee));
    }

    @AfterEach
    void close() {
        mockStatic.close();
    }

//    @ParameterizedTest
//    @MethodSource("provide")
//    void getMonthEndReportForUser_DayBefore14thInMonth_InitialDatePreviousMonth(YearMonth now, Month expectedMonth) {
//        //GIVEN
//        final Employee employee = createEmployeeWithReleaseDate(0, "NULL");
//        when(zepService.getEmployee(anyString())).thenReturn(employee);
//
//        //WHEN
//        var result = monthlyReportService.getMonthEndReportForUser(employee, null);
//
//        //THEN
//        assertThat(result.getInitialDate().getMonth()).isEqualTo(expectedMonth);
//    }
//
//    private Stream<Arguments> provide() {
//        return Stream.of(
//                Arguments.of(YearMonth.of(2024, 12), "NOVEMBER"),
//                Arguments.of(YearMonth.of(2025, 1), "DECEMBER")
//        );
//    }

    @Test
    void getMonthendReportForUser_MitarbeiterValid() {
        assertThat(monthlyReportService.getMonthEndReportForUser(YearMonth.now()))
                .isNotNull();
    }

    @Test
    void getMonthendReportForUser_MitarbeiterValid_ProjektzeitenValid() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(new ArrayList<>());
        when(warningCalculatorsManager.determineNoTimeEntries(any(Employee.class), anyList(), anyList())).thenReturn(new ArrayList<>());

        assertThat(monthlyReportService.getMonthEndReportForUser(YearMonth.now()))
                .isNotNull();
    }

    @Test
    void getMonthendReportForUser_MitarbeiterValid_ProjektzeitenValid_NoWarning() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjektzeitenResponseType(10));
        when(warningCalculatorsManager.determineNoTimeEntries(any(Employee.class), anyList(), anyList())).thenReturn(new ArrayList<>());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.of(2022, 4));

        assertThat(monthendReportForUser)
                .isNotNull();
        assertThat(monthendReportForUser.getEmployee().getEmail())
                .isEqualTo("max.mustermann@gepardec.com");
        assertThat(monthendReportForUser.getTimeWarnings())
                .isNotNull();
        assertThat(monthendReportForUser.getTimeWarnings().isEmpty())
                .isTrue();
    }

    @Test
    void getMonthendReportForUser_MitarbeiterValid_ProjektzeitenValid_Warning() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjektzeitenResponseType(18));
        when(zepService.getAbsenceForEmployee(any(Employee.class), any(YearMonth.class))).thenReturn(new ArrayList<>());
        when(warningCalculatorsManager.determineTimeWarnings(anyList())).thenReturn(createTimeWarningList());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.of(2022, 4));

        assertThat(monthendReportForUser)
                .isNotNull();
        assertThat(monthendReportForUser.getEmployee().getEmail())
                .isEqualTo("max.mustermann@gepardec.com");
        assertThat(monthendReportForUser.getTimeWarnings())
                .isNotNull();
        assertThat(Objects.requireNonNull(monthendReportForUser.getTimeWarnings()).isEmpty())
                .isFalse();
        assertThat(monthendReportForUser.getTimeWarnings().get(0).getDate())
                .isEqualTo(LocalDate.of(2020, 1, 31));
    }

    @Test
    void getMonthendReportForUser_isUserIsValidAndHasNursingAbsenceDays_thenReturnsReportWithCorrectNursingDays() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjektzeitenResponseType(18));
        List<AbsenceTime> absenceList = new ArrayList<>();
        AbsenceTime nursingDay = new AbsenceTime(
                "0",
                LocalDate.of(2020, 2, 27),
                LocalDate.of(2020, 2, 29),
                "PU",
                true

        );

        absenceList.add(nursingDay);
        when(zepService.getAbsenceForEmployee(any(Employee.class), any(YearMonth.class))).thenReturn(absenceList);
        when(warningCalculatorsManager.determineTimeWarnings(anyList())).thenReturn(new ArrayList<>());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.of(2022, 2));

        assertAll(
                () -> assertThat(monthendReportForUser)
                        .isNotNull(),
                () -> assertThat(monthendReportForUser.getEmployee().getEmail())
                        .isEqualTo("max.mustermann@gepardec.com"),
                () -> assertThat(monthendReportForUser.getTimeWarnings())
                        .isNotNull(),
                () -> assertThat(Objects.requireNonNull(monthendReportForUser.getTimeWarnings()).isEmpty())
                        .isTrue(),
                () -> assertThat(monthendReportForUser.getNursingDays()).isEqualTo(2)
        );
    }

    @Test
    void getMonthendReportForUser_isUserIsValidAndHasMaternityLeaveAbsenceDays_thenReturnsReportWithCorrectAmountOfMaternityLeaveDays() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjektzeitenResponseType(18));
        List<AbsenceTime> absenceList = new ArrayList<>();
        AbsenceTime maternityLeaveDay = new AbsenceTime(
                "0",
                LocalDate.of(2020, 2, 27),
                LocalDate.of(2020, 2, 29),
                "KA",
                true
        );
        absenceList.add(maternityLeaveDay);
        when(zepService.getAbsenceForEmployee(any(Employee.class), any(YearMonth.class))).thenReturn(absenceList);
        when(warningCalculatorsManager.determineTimeWarnings(anyList())).thenReturn(new ArrayList<>());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.of(2022, 2));

        assertAll(
                () -> assertThat(monthendReportForUser)
                        .isNotNull(),
                () -> assertThat(monthendReportForUser.getEmployee().getEmail())
                        .isEqualTo("max.mustermann@gepardec.com"),
                () -> assertThat(monthendReportForUser.getTimeWarnings())
                        .isNotNull(),
                () -> assertThat(Objects.requireNonNull(monthendReportForUser.getTimeWarnings()))
                        .isEmpty(),
                () -> assertThat(monthendReportForUser.getMaternityLeaveDays()).isEqualTo(2)
        );
    }

    @Test
    void getMonthendReportForUser_isUserIsValidAndHasExternalTrainingAbsenceDays_thenReturnsReportWithCorrectAmountOfExternalTrainingDays() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjektzeitenResponseType(18));
        List<AbsenceTime> absenceList = new ArrayList<>();
        AbsenceTime externalTrainingAbsence = createAbsenceFromType("EW");
        absenceList.add(externalTrainingAbsence);
        when(zepService.getAbsenceForEmployee(any(Employee.class), any(YearMonth.class))).thenReturn(absenceList);
        when(warningCalculatorsManager.determineTimeWarnings(anyList())).thenReturn(new ArrayList<>());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.of(2022, 2));

        assertAll(
                () -> assertThat(monthendReportForUser)
                        .isNotNull(),
                () -> assertThat(monthendReportForUser.getEmployee().getEmail())
                        .isEqualTo("max.mustermann@gepardec.com"),
                () -> assertThat(monthendReportForUser.getTimeWarnings())
                        .isNotNull(),
                () -> assertThat(Objects.requireNonNull(monthendReportForUser.getTimeWarnings()))
                        .isEmpty(),
                () -> assertThat(monthendReportForUser.getExternalTrainingDays()).isEqualTo(2)
        );
    }

    @Test
    void getMonthendReportForUser_isUserIsValidAndHasConferenceAbsenceDays_thenReturnsReportWithCorrectAmountOfConferenceDays() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjektzeitenResponseType(18));
        List<AbsenceTime> absenceList = new ArrayList<>();
        AbsenceTime conferenceDaysAbsence = createAbsenceFromType("KO");
        absenceList.add(conferenceDaysAbsence);
        when(zepService.getAbsenceForEmployee(any(Employee.class), any(YearMonth.class))).thenReturn(absenceList);
        when(warningCalculatorsManager.determineTimeWarnings(anyList())).thenReturn(new ArrayList<>());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.of(2022, 2));

        assertAll(
                () -> assertThat(monthendReportForUser)
                        .isNotNull(),
                () -> assertThat(monthendReportForUser.getEmployee().getEmail())
                        .isEqualTo("max.mustermann@gepardec.com"),
                () -> assertThat(monthendReportForUser.getTimeWarnings())
                        .isNotNull(),
                () -> assertThat(Objects.requireNonNull(monthendReportForUser.getTimeWarnings()))
                        .isEmpty(),
                () -> assertThat(monthendReportForUser.getConferenceDays()).isEqualTo(2)
        );
    }

    @Test
    void getMonthendReportForUser_isUserIsValidAndHasMaternityProtectionAbsenceDays_thenReturnsReportWithCorrectAmountOfMaternityProtectionDays() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjektzeitenResponseType(18));
        List<AbsenceTime> absenceList = new ArrayList<>();
        AbsenceTime maternityProtectionDaysAbsence = createAbsenceFromType("MU");
        absenceList.add(maternityProtectionDaysAbsence);
        when(zepService.getAbsenceForEmployee(any(Employee.class), any(YearMonth.class))).thenReturn(absenceList);
        when(warningCalculatorsManager.determineTimeWarnings(anyList())).thenReturn(new ArrayList<>());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.of(2022, 2));

        assertAll(
                () -> assertThat(monthendReportForUser)
                        .isNotNull(),
                () -> assertThat(monthendReportForUser.getEmployee().getEmail())
                        .isEqualTo("max.mustermann@gepardec.com"),
                () -> assertThat(monthendReportForUser.getTimeWarnings())
                        .isNotNull(),
                () -> assertThat(Objects.requireNonNull(monthendReportForUser.getTimeWarnings()))
                        .isEmpty(),
                () -> assertThat(monthendReportForUser.getMaternityProtectionDays()).isEqualTo(2)
        );
    }

    @Test
    void getMonthendReportForUser_isUserIsValidAndHasFatherMonthAbsenceDays_thenReturnsReportWithCorrectAmountOfFatherMonthDays() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjektzeitenResponseType(18));
        List<AbsenceTime> absenceList = new ArrayList<>();
        AbsenceTime fatherMonthDaysAbsence = createAbsenceFromType("PA");
        absenceList.add(fatherMonthDaysAbsence);
        when(zepService.getAbsenceForEmployee(any(Employee.class), any(YearMonth.class))).thenReturn(absenceList);
        when(warningCalculatorsManager.determineTimeWarnings(anyList())).thenReturn(new ArrayList<>());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.of(2022, 2));

        assertAll(
                () -> assertThat(monthendReportForUser)
                        .isNotNull(),
                () -> assertThat(monthendReportForUser.getEmployee().getEmail())
                        .isEqualTo("max.mustermann@gepardec.com"),
                () -> assertThat(monthendReportForUser.getTimeWarnings())
                        .isNotNull(),
                () -> assertThat(Objects.requireNonNull(monthendReportForUser.getTimeWarnings()))
                        .isEmpty(),
                () -> assertThat(monthendReportForUser.getFatherMonthDays()).isEqualTo(2)
        );
    }

    @Test
    void getMonthendReportForUser_isUserIsValidAndHasPaidSpecialLeaveAbsenceDays_thenReturnsReportWithCorrectAmountOfPaidSpecialLeaveDays() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjektzeitenResponseType(18));
        List<AbsenceTime> absenceList = new ArrayList<>();
        AbsenceTime paidSpecialLeaveDaysAbsence = createAbsenceFromType("SU");
        absenceList.add(paidSpecialLeaveDaysAbsence);
        when(zepService.getAbsenceForEmployee(any(Employee.class), any(YearMonth.class))).thenReturn(absenceList);
        when(warningCalculatorsManager.determineTimeWarnings(anyList())).thenReturn(new ArrayList<>());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.of(2022, 2));

        assertAll(
                () -> assertThat(monthendReportForUser)
                        .isNotNull(),
                () -> assertThat(monthendReportForUser.getEmployee().getEmail())
                        .isEqualTo("max.mustermann@gepardec.com"),
                () -> assertThat(monthendReportForUser.getTimeWarnings())
                        .isNotNull(),
                () -> assertThat(Objects.requireNonNull(monthendReportForUser.getTimeWarnings()))
                        .isEmpty(),
                () -> assertThat(monthendReportForUser.getPaidSpecialLeaveDays()).isEqualTo(2)
        );
    }

    @Test
    void getMonthendReportForUser_isUserIsValidAndHasNonPaidVacationAbsenceDays_thenReturnsReportWithCorrectAmountOfNonPaidVacationDays() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjektzeitenResponseType(18));
        List<AbsenceTime> absenceList = new ArrayList<>();
        AbsenceTime nonPaidVacationDaysAbsence = createAbsenceFromType("UU");
        absenceList.add(nonPaidVacationDaysAbsence);
        when(zepService.getAbsenceForEmployee(any(Employee.class), any(YearMonth.class))).thenReturn(absenceList);
        when(warningCalculatorsManager.determineTimeWarnings(anyList())).thenReturn(new ArrayList<>());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.of(2022, 2));

        assertAll(
                () -> assertThat(monthendReportForUser)
                        .isNotNull(),
                () -> assertThat(monthendReportForUser.getEmployee().getEmail())
                        .isEqualTo("max.mustermann@gepardec.com"),
                () -> assertThat(monthendReportForUser.getTimeWarnings())
                        .isNotNull(),
                () -> assertThat(Objects.requireNonNull(monthendReportForUser.getTimeWarnings()))
                        .isEmpty(),
                () -> assertThat(monthendReportForUser.getNonPaidVacationDays()).isEqualTo(2)
        );
    }

    @Test
    void getMonthendReportForUser_isUserValidAndHasPaidVacationOverWeekend_thenReturnsReportWithOnlyVacationDaysOnWorkdays() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjectTimesResponseTypeForCorrectVacationDays());
        when(zepService.getAbsenceForEmployee(any(Employee.class), any(YearMonth.class))).thenReturn(createVacationAbsenceList());
        when(warningCalculatorsManager.determineTimeWarnings(anyList())).thenReturn(new ArrayList<>());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.of(2020, 2));

        assertAll(
                () -> assertThat(monthendReportForUser).isNotNull(),
                () -> assertThat(monthendReportForUser.getVacationDays()).isEqualTo(2)
        );
    }

    @Test
    void getMonthendReportForUser_WithYearAndMonth_isUserValidAndHasPaidVacationOverWeekendWhichExtendsOverMonthEnd_thenReturnsReportWithOnlyVacationDaysOnWorkdays() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjectTimesResponseTypeForCorrectVacationDays());
        when(zepService.getAbsenceForEmployee(any(Employee.class), any(YearMonth.class))).thenReturn(createVacationAbsenceListWhichExtendsOverMonthEnd());
        when(warningCalculatorsManager.determineTimeWarnings(anyList())).thenReturn(new ArrayList<>());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.of(2022, 4));

        assertAll(
                () -> assertThat(monthendReportForUser).isNotNull(),
                () -> assertThat(monthendReportForUser.getVacationDays()).isEqualTo(5)
        );
    }

    @Test
    @Disabled("Needs to be reworked with mocked services etc.")
    void getMonthendReportForUser_isUserValidAndHasPaidVacationOverWeekendWhichExtendsOverMonthEnd_thenReturnsReportWithOnlyVacationDaysOnWorkdays() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjectTimesResponseTypeForCorrectVacationDays());

        List<AbsenceTime> absenceList = new ArrayList<>();

        var vacationDaysAbsenceBuilder = AbsenceTime.builder()
                .reason(AbsenteeType.VACATION_DAYS.getType())
                .accepted(true);

        LocalDate firstOfCurrentMonth = LocalDate.now();
        LocalDate firstOfLastMonth = LocalDate.now().withMonth(firstOfCurrentMonth.getMonthValue() - 1);
        LocalDate midOfMonth = LocalDate.now().withDayOfMonth(14);
        LocalDate startDate;
        LocalDate endDate;

        if (firstOfCurrentMonth.isAfter(midOfMonth)) {
            startDate = firstOfCurrentMonth.withDayOfMonth(1);
            endDate = firstOfCurrentMonth.withDayOfMonth(10);
        } else {
            startDate = firstOfLastMonth.withDayOfMonth(1);
            endDate = firstOfLastMonth.withDayOfMonth(10);
        }

        vacationDaysAbsenceBuilder.fromDate(startDate);
        vacationDaysAbsenceBuilder.toDate(endDate);
        absenceList.add(vacationDaysAbsenceBuilder.build());

        when(zepService.getAbsenceForEmployee(any(Employee.class), any(YearMonth.class))).thenReturn(absenceList);
        when(warningCalculatorsManager.determineTimeWarnings(anyList())).thenReturn(new ArrayList<>());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.now());

        assertAll(
                () -> assertThat(monthendReportForUser).isNotNull(),
                () -> assertThat(monthendReportForUser.getVacationDays()).isGreaterThan(4)
        );
    }

    @Test
    void getMonthendReportForUser_isUserValidAndHasHomeofficeOverWeekend_thenReturnsReportWithHomeOfficeOnWorkdays() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjectTimesResponseTypeForCorrectVacationDays());
        when(zepService.getAbsenceForEmployee(any(Employee.class), any(YearMonth.class))).thenReturn(createHomeOfficeListWhichExtendsOverWeekend());
        when(warningCalculatorsManager.determineTimeWarnings(anyList())).thenReturn(new ArrayList<>());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.of(2020, 2));

        assertAll(
                () -> assertThat(monthendReportForUser).isNotNull(),
                () -> assertThat(monthendReportForUser.getHomeofficeDays()).isEqualTo(2),
                () -> assertThat(monthendReportForUser.getTimeWarnings()).isEmpty()
        );
    }

    @Test
    void getMonthendReportForUser_isUserValidAndHasHomeofficeOverWeekendAndExtendsOverMonth_thenReturnsReportWithHomeOfficeOnWorkdays() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjectTimesResponseTypeForCorrectVacationDays());
        when(zepService.getAbsenceForEmployee(any(Employee.class), any(YearMonth.class))).thenReturn(createHomeOfficeListWhichExtendsOverMonth());
        when(warningCalculatorsManager.determineTimeWarnings(anyList())).thenReturn(new ArrayList<>());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.of(2022, 4));

        assertAll(
                () -> assertThat(monthendReportForUser).isNotNull(),
                () -> assertThat(monthendReportForUser.getHomeofficeDays()).isEqualTo(5),
                () -> assertThat(monthendReportForUser.getTimeWarnings()).isEmpty()
        );
    }

    @Test
    void getMonthendReportForUser_isUserValidAndHasTimeCompensationOverWeekend_thenReturnsReportWithCorrectTimeCompensationOnWorkdays() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjectTimesResponseTypeForCorrectVacationDays());
        when(zepService.getAbsenceForEmployee(any(Employee.class), any(YearMonth.class))).thenReturn(createTimeCompensationWhichExtendsOverWeekend());
        when(warningCalculatorsManager.determineTimeWarnings(anyList())).thenReturn(new ArrayList<>());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.of(2022, 4));

        assertAll(
                () -> assertThat(monthendReportForUser).isNotNull(),
                () -> assertThat(monthendReportForUser.getCompensatoryDays()).isEqualTo(5),
                () -> assertThat(monthendReportForUser.getTimeWarnings()).isEmpty()
        );
    }

    @Test
    void getMonthendReportForUser_isUserValidAndHasTimeCompensationOverWeekendAndExtendsOverMonth_thenReturnsReportWithCorrectTimeCompensationOnWorkdays() {
        when(zepService.getProjectTimes(any(Employee.class), any(YearMonth.class))).thenReturn(createReadProjectTimesResponseTypeForCorrectVacationDays());
        when(zepService.getAbsenceForEmployee(any(Employee.class), any(YearMonth.class))).thenReturn(createTimeCompensationWhichExtendsOverWeekendAndMonth());
        when(warningCalculatorsManager.determineTimeWarnings(anyList())).thenReturn(new ArrayList<>());

        final MonthlyReport monthendReportForUser = monthlyReportService.getMonthEndReportForUser(YearMonth.of(2022, 4));

        assertAll(
                () -> assertThat(monthendReportForUser).isNotNull(),
                () -> assertThat(monthendReportForUser.getCompensatoryDays()).isEqualTo(5),
                () -> assertThat(monthendReportForUser.getTimeWarnings()).isEmpty()
        );
    }

    private List<AbsenceTime> createVacationAbsenceList() {
        List<AbsenceTime> absenceList = new ArrayList<>();

        AbsenceTime vacationDaysAbsence = createAbsenceFromType("UB");
        absenceList.add(vacationDaysAbsence);

        return absenceList;
    }

    private List<AbsenceTime> createHomeOfficeListWhichExtendsOverWeekend() {
        List<AbsenceTime> absenceList = new ArrayList<>();

        AbsenceTime vacationDaysAbsence = createAbsenceFromType("HO");
        absenceList.add(vacationDaysAbsence);

        return absenceList;
    }

    private List<AbsenceTime> createHomeOfficeListWhichExtendsOverMonth() {
        List<AbsenceTime> absenceList = new ArrayList<>();

        AbsenceTime vacationDaysAbsence = AbsenceTime.builder()
                .reason("HO")
                .accepted(true)
                .toDate(LocalDate.of(2022, 5, 3))
                .fromDate(LocalDate.of(2022, 4, 25))
                .build();
        absenceList.add(vacationDaysAbsence);

        return absenceList;
    }

    private List<AbsenceTime> createTimeCompensationWhichExtendsOverWeekend() {
        List<AbsenceTime> absenceList = new ArrayList<>();

        AbsenceTime vacationDaysAbsence = AbsenceTime.builder()
                .reason(AbsenteeType.COMPENSATORY_DAYS.getType())
                .accepted(true)
                .toDate(LocalDate.of(2022, 4, 29))
                .fromDate(LocalDate.of(2022, 4, 25))
                .build();
        absenceList.add(vacationDaysAbsence);

        return absenceList;
    }

    private List<AbsenceTime> createTimeCompensationWhichExtendsOverWeekendAndMonth() {
        List<AbsenceTime> absenceList = new ArrayList<>();

        AbsenceTime vacationDaysAbsence = AbsenceTime.builder()
                .reason(AbsenteeType.COMPENSATORY_DAYS.getType())
                .accepted(true)
                .toDate(LocalDate.of(2022, 5, 3))
                .fromDate(LocalDate.of(2022, 4, 25))
                .build();
        absenceList.add(vacationDaysAbsence);

        return absenceList;
    }

    private List<AbsenceTime> createVacationAbsenceListWhichExtendsOverMonthEnd() {
        List<AbsenceTime> absenceList = new ArrayList<>();

        AbsenceTime vacationDaysAbsence = AbsenceTime.builder()
                .reason(AbsenteeType.VACATION_DAYS.getType())
                .accepted(true)
                .toDate(LocalDate.of(2022, 5, 3))
                .fromDate(LocalDate.of(2022, 4, 25))
                .build();
        absenceList.add(vacationDaysAbsence);

        return absenceList;
    }

    private List<ProjectEntry> createReadProjectTimesResponseTypeForCorrectVacationDays() {
        List<Integer> weekEndDays = List.of(2, 3, 9, 10, 16, 17, 18, 19, 20);
        List<ProjectEntry> projectTimeEntryList = new ArrayList<>();

        for (int i = 1; i <= 22; i++) {
            if (!weekEndDays.contains(i)) {
                projectTimeEntryList.add(ProjectTimeEntry.builder()
                        .fromTime(LocalDateTime.of(2022, 4, i, 8, 0))
                        .toTime(LocalDateTime.of(2022, 4, i, 16, 30))
                        .task(Task.BEARBEITEN)
                        .workingLocation(WorkingLocation.MAIN).build());
            }
        }

        return projectTimeEntryList;
    }

    private List<TimeWarning> createTimeWarningList() {
        TimeWarning timewarning = new TimeWarning();
        timewarning.setDate(LocalDate.of(2020, 1, 31));
        timewarning.getWarningTypes().add(TimeWarningType.OUTSIDE_CORE_WORKING_TIME);
        timewarning.setExcessWorkTime(1d);
        timewarning.setMissingBreakTime(0.5d);
        List<TimeWarning> timeWarningList = new ArrayList<>();
        timeWarningList.add(timewarning);

        return timeWarningList;
    }

    private List<ProjectEntry> createReadProjektzeitenResponseType(int bisHours) {

        return List.of(
                ProjectTimeEntry.builder()
                        .fromTime(LocalDateTime.of(2020, 1, 31, 7, 0))
                        .toTime(LocalDateTime.of(2020, 1, 31, bisHours, 0))
                        .task(Task.BEARBEITEN)
                        .workingLocation(WorkingLocation.MAIN).build(),
                ProjectTimeEntry.builder()
                        .fromTime(LocalDateTime.of(2020, 1, 30, 7, 0))
                        .toTime(LocalDateTime.of(2020, 1, 30, 10, 0))
                        .task(Task.BEARBEITEN)
                        .workingLocation(WorkingLocation.MAIN).build()
        );
    }

    private Employee createEmployeeForUser(final User user) {
        Map<DayOfWeek, Duration> regularWorkingHours = Map.ofEntries(
                Map.entry(DayOfWeek.MONDAY, Duration.ofHours(8)),
                Map.entry(DayOfWeek.TUESDAY, Duration.ofHours(8)),
                Map.entry(DayOfWeek.WEDNESDAY, Duration.ofHours(8)),
                Map.entry(DayOfWeek.THURSDAY, Duration.ofHours(8)),
                Map.entry(DayOfWeek.FRIDAY, Duration.ofHours(6)),
                Map.entry(DayOfWeek.SATURDAY, Duration.ofHours(0)),
                Map.entry(DayOfWeek.SUNDAY, Duration.ofHours(0)));

        return Employee.builder()
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .title("Ing.")
                .userId(user.getUserId())
                .releaseDate("2020-01-01")
                .employmentPeriods(new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2020, 1, 1), null)))
                .regularWorkingTimes(new RegularWorkingTimes(new RegularWorkingTime(null, regularWorkingHours)))
                .build();
    }

    private User createUser() {
        return User.builder()
                .dbId(1)
                .userId("1")
                .email("max.mustermann@gepardec.com")
                .firstname("Max")
                .lastname("Mustermann")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
    }

    private AbsenceTime createAbsenceFromType(String type) {
        return AbsenceTime.builder()
                .reason(type)
                .accepted(true)
                .toDate(LocalDate.of(2020, 2, 29))
                .fromDate(LocalDate.of(2020, 2, 27))
                .build();
    }
}
