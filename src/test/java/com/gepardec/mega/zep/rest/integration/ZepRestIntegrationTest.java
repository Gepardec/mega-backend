package com.gepardec.mega.zep.rest.integration;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.impl.Rest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

//Integration tests for the getEmployee method in ZepRestService
@QuarkusTest
@Disabled
class ZepRestIntegrationTest {


    @Inject
    @Rest
    ZepService zepService;

    @Test
    void fetchValidEmployee_thenReturnEmployee() {

        Employee expected = Employee.builder()
                .userId("001-hwirnsberger")
                .email(null)
                .title("BSc")
                .firstname("Herbert")
                .lastname("Wirnsberger")
                .salutation("Sir")
                .releaseDate("2021-02-28")
                .workDescription("06")
                .language(null)
                .regularWorkingHours(Map.of(DayOfWeek.MONDAY, Duration.ofHours(6),
                        DayOfWeek.TUESDAY, Duration.ofHours(6),
                        DayOfWeek.WEDNESDAY, Duration.ofHours(6),
                        DayOfWeek.THURSDAY, Duration.ofHours(6),
                        DayOfWeek.FRIDAY, Duration.ofHours(6),
                        DayOfWeek.SATURDAY, Duration.ofHours(0),
                        DayOfWeek.SUNDAY, Duration.ofHours(0)))
                .active(true)
                .exitDate(null)
                .build();

        Employee actual = zepService.getEmployee("001-hwirnsberger");

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void getEmployees_InvalidUsername_thenThrowException() {
        assertThat(zepService.getEmployee("001-testtestnothere")).isNull();
    }

    @Test
    void getProjectTimes_valid() {
        List<ProjectEntry> projectEntries = zepService.getProjectTimes(zepService.getEmployee("001-hwirnsberger"), YearMonth.of(2020, 4));
        assertThat(projectEntries.size()).isPositive();
        for (ProjectEntry projectEntry : projectEntries) {
            assertThat(projectEntry.getDurationInHours()).isNotNull();
            assertThat(projectEntry.getDate().getMonthValue()).isEqualTo(4);
        }
    }

    @Test
    void getProjectTimes_noEntries() {
        List<ProjectEntry> projectEntries = zepService.getProjectTimes(zepService.getEmployee("082-tmeindl"), YearMonth.of(2020, 4));
        assertThat(projectEntries.size()).isZero();
    }

    @Test
    void getProjectTimesForEmployeePerProject_valid() {
        List<ProjectTime> projectTimes = zepService.getProjectTimesForEmployeePerProject("BVAEB-KAP-2021", YearMonth.of(2021, 1));
        assertThat(projectTimes.size()).isEqualTo(4);
        for (ProjectTime projectTime : projectTimes) {
            assertThat(projectTime.getDuration()).isNotNull();
        }
    }

    @Test
    void getProjectsForMonthYear_valid() {
        for (int i = 1; i < 10; i++) {
            List<Project> projects = zepService.getProjectsForMonthYear(YearMonth.of(2020, i));
            assertThat(projects.size()).isPositive();
        }
        List<Project> projects = zepService.getProjectsForMonthYear(YearMonth.of(2021, 1));
        assertThat(projects.size()).isEqualTo(33);
        for (Project project : projects) {
            assertThat(project.getStartDate()).isBefore(LocalDate.of(2021, 2, 1));
        }
    }

    @Test
    void getProjectForFuture_then() {
        List<Project> projects = zepService.getProjectsForMonthYear(YearMonth.of(2033, 1));
        for (Project project : projects) {
            //TODO Wait for ProjectMapper todo to be resolved
        }
    }

    @Test
    void getProjectByName_valid() {
        Project project = zepService.getProjectByName("BVAEB-KAP-2021", YearMonth.of(2021, 1)).get();
        assertThat(project.getZepId()).isEqualTo(158);
    }
}
