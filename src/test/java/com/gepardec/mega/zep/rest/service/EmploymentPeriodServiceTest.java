package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fasterxml.jackson.databind.type.LogicalType.DateTime;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class EmploymentPeriodServiceTest {

    @Inject
    EmploymentPeriodService employmentPeriodService;

    @Test
    public void employmentPeriodFromUser() {
        String[] referenceEndDatesArr = {
                "2017-09-08T00:00:00.000000Z",
                "2019-12-31T00:00:00.000000Z",
                "2024-01-17T00:00:00.000000Z"
        };

        List<LocalDateTime> referenceEndDates = Arrays.stream(referenceEndDatesArr)
                .map(dateTimeString -> LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME))
                .sorted()
                .collect(Collectors.toList());

        ZepEmploymentPeriod[] employmentPeriods = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName("032-melBanna");
        Stream<LocalDateTime> endDateTimes = Arrays.stream(employmentPeriods)
                .map(ZepEmploymentPeriod::getEndDate)
                .sorted();

        assertThat(endDateTimes.allMatch(referenceEndDates::contains)).isTrue();
    }

}
