package com.gepardec.mega.notification.mail.dates;

import com.gepardec.mega.notification.mail.Mail;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.gepardec.mega.notification.mail.Mail.EMPLOYEE_CHECK_PROJECTTIME;
import static com.gepardec.mega.notification.mail.Mail.OM_ADMINISTRATIVE;
import static com.gepardec.mega.notification.mail.Mail.OM_CONTROL_EMPLOYEES_CONTENT;
import static com.gepardec.mega.notification.mail.Mail.OM_CONTROL_PROJECTTIMES;
import static com.gepardec.mega.notification.mail.Mail.OM_RELEASE;
import static com.gepardec.mega.notification.mail.Mail.OM_SALARY;
import static com.gepardec.mega.notification.mail.Mail.PL_PROJECT_CONTROLLING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@QuarkusTest
class BusinessDayCalculatorTest {

    @Inject
    BusinessDayCalculator businessDayCalculator;

    @Test
    void getRemindersForDate_FirstDayOfMonthBusinessDay_EmptyList() {
        assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 10, 1)));
    }

    @Test
    void getRemindersForDate_FirstDayOfMonthNoBusinessDay_EmptyList() {
        assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 1)));
    }

    @Test
    void getRemindersForDate_LastDayOfMonthBusinessDay_EmployeeCheckProjectTimeAndOmControlProjectTimes() {
        //GIVEN
        var currentDate = LocalDate.of(2023, 8, 31);

        //WHEN
        var actualReminders = businessDayCalculator.getRemindersForDate(currentDate);

        //THEN
        assertRemindersEqual(
                List.of(EMPLOYEE_CHECK_PROJECTTIME, OM_CONTROL_PROJECTTIMES),
                actualReminders
        );
    }

    @Test
    void getRemindersForDate_LastDayOfMonthNotBusinessDay_EmptyList() {
        //GIVEN
        var currentDate = LocalDate.of(2023, 4, 30); // = Sunday

        //WHEN
        var actualReminders = businessDayCalculator.getRemindersForDate(currentDate);

        //THEN
        assertRemindersEmpty(actualReminders);
    }

    @Test
    void getRemindersForDate_LastDayOfMonthNotBusinessDay_EmployeeCheckProjectTimeAndOmControlProjectTimesOnFriday() {
        //GIVEN
        // 2023-04-30 is a sunday => 2023-04-28 is last working day of the month
        var currentDate = LocalDate.of(2023, 4, 28); // = Friday

        //WHEN
        var actualReminders = businessDayCalculator.getRemindersForDate(currentDate);

        //THEN
        assertRemindersEqual(
                List.of(EMPLOYEE_CHECK_PROJECTTIME, OM_CONTROL_PROJECTTIMES),
                actualReminders
        );
    }

    @Test
    void getRemindersForDate_adminstrativesOn15thDayOfMonthIsWorkday_shouldReturnReminderOn15th() {
        assertRemindersEqual(List.of(OM_ADMINISTRATIVE), businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 4, 15)));
    }

    @Test
    void getRemindersForDate_adminstrativesOn15thDayOfMonthIsNoWorkday_shouldReturnReminderOn15th() {
        assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 15)));
        assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 16)));
        assertRemindersEqual(List.of(OM_ADMINISTRATIVE), businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 17)));
    }

    @Test
    void getRemindersForDate_differentDatesOfMonthNovember_shouldReturnCorrectReminder() {
        assertAll(
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 1))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 2))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 3))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 4))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 5))),
                () -> assertRemindersEqual(List.of(OM_CONTROL_EMPLOYEES_CONTENT), businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 6))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 7))),
                () -> assertRemindersEqual(List.of(PL_PROJECT_CONTROLLING), businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 8))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 9))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 10))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 11))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 12))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 13))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 14))),
                () -> assertRemindersEqual(List.of(OM_ADMINISTRATIVE), businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 15))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 16))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 17))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 18))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 19))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 20))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 21))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 22))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 23))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 24))),
                () -> assertRemindersEqual(List.of(OM_RELEASE), businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 25))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 26))),
                () -> assertRemindersEqual(List.of(OM_SALARY), businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 27))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 28))),
                () -> assertRemindersEqual(List.of(EMPLOYEE_CHECK_PROJECTTIME, OM_CONTROL_PROJECTTIMES), businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 29))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2019, 11, 30)))
        );
    }

    @Test
    void getRemindersForDate_differentDatesOfMonthFebruary_shouldReturnCorrectReminder() {
        assertAll(
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 1))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 2))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 3))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 4))),
                () -> assertRemindersEqual(List.of(OM_CONTROL_EMPLOYEES_CONTENT), businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 5))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 6))),
                () -> assertRemindersEqual(List.of(PL_PROJECT_CONTROLLING), businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 7))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 8))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 9))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 10))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 11))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 12))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 13))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 14))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 15))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 16))),
                () -> assertRemindersEqual(List.of(OM_ADMINISTRATIVE), businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 17))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 18))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 19))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 20))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 21))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 22))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 23))),
                () -> assertRemindersEqual(List.of(OM_RELEASE), businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 24))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 25))),
                () -> assertRemindersEqual(List.of(OM_SALARY), businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 26))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 27))),
                () -> assertRemindersEqual(List.of(EMPLOYEE_CHECK_PROJECTTIME, OM_CONTROL_PROJECTTIMES), businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 28))),
                () -> assertRemindersEmpty(businessDayCalculator.getRemindersForDate(LocalDate.of(2020, 2, 29)))
        );
    }

    @Test
    void removeWorkingDaysFromNextMonth_positiveInt() {
        LocalDate localDate = businessDayCalculator.removeWorkingdaysFromNextMonth(LocalDate.of(2022, 1, 10), 20);
        assertThat(localDate).isEqualTo(LocalDate.of(2022, 1, 3));
    }

    @Test
    void removeWorkingDaysFromNextMonth_negativeInt() {
        LocalDate localDate = businessDayCalculator.removeWorkingdaysFromNextMonth(LocalDate.of(2022, 1, 10), -20);
        assertThat(localDate).isEqualTo(LocalDate.of(2022, 1, 3));
    }

    @Test
    void addWorkingDays_0_returnsInputDate() {
        LocalDate date = businessDayCalculator.addWorkingdays(LocalDate.of(2022, 1, 3), 0);
        assertThat(date).isEqualTo(LocalDate.of(2022, 1, 3));
    }

    @Test
    void addWorkingDays_20_returns20220201() {
        LocalDate date = businessDayCalculator.addWorkingdays(LocalDate.of(2022, 1, 3), 20);
        assertThat(date).isEqualTo(LocalDate.of(2022, 2, 1));
    }

    private void assertRemindersEmpty(List<Mail> actualReminders) {
        assertThat(actualReminders).isEmpty();
    }

    private void assertRemindersEqual(List<Mail> expectedReminders, List<Mail> actualReminders) {
        assertThat(actualReminders).isEqualTo(expectedReminders);
    }
}
