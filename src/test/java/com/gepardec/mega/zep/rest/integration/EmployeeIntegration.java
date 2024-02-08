package com.gepardec.mega.zep.rest.integration;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.impl.Rest;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


//Integration tests for the getEmployee method in ZepRestService
@QuarkusTest
public class EmployeeIntegration {


    @Inject @Rest
    ZepService zepService;


    @Test
    @Disabled
    public void fetchValidEmployee_thenReturnEmployee() {

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
}
