package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class EmployeeMapperTest {

    @Test
    public void getActiveWhen_3DatesActive() {
        ZepEmploymentPeriod[] employmentPeriods = {
                ZepEmploymentPeriod.builder()
                        .endDate(LocalDateTime.of(2018, 2, 1, 12, 32, 12))
                        .build(),
                ZepEmploymentPeriod.builder()
                        .endDate(LocalDateTime.of(2022, 12, 2, 23, 17, 4))
                        .build(),
                ZepEmploymentPeriod.builder()
                        .endDate(null)
                        .build()
        };

        boolean active = EmployeeMapper.getActiveOfZepEmploymentPeriods(employmentPeriods);
        assertThat(active).isTrue();

    }
    @Test
    public void getInactiveWhen_lastDateInactive() {
        ZepEmploymentPeriod[] employmentPeriods = {
                ZepEmploymentPeriod.builder()
                        .endDate(LocalDateTime.of(2018, 2, 1, 12, 32, 12))
                        .build(),
                ZepEmploymentPeriod.builder()
                        .endDate(LocalDateTime.of(2022, 12, 2, 23, 17, 4))
                        .build(),
                ZepEmploymentPeriod.builder()
                        .endDate(LocalDateTime.of(2023, 11, 12, 3, 1, 2))
                        .build(),
        };

        boolean active = EmployeeMapper.getActiveOfZepEmploymentPeriods(employmentPeriods);
        assertThat(active).isFalse();
    }
    @Test
    public void getActiveWhen_3Dates_firstDateInactive() {
        ZepEmploymentPeriod[] employmentPeriods = {
                ZepEmploymentPeriod.builder()
                        .endDate(null)
                        .build(),
                ZepEmploymentPeriod.builder()
                        .endDate(LocalDateTime.of(2022, 12, 2, 23, 17, 4))
                        .build(),
                ZepEmploymentPeriod.builder()
                        .endDate(LocalDateTime.of(2018, 2, 1, 12, 32, 12))
                        .build()

        };

        boolean active = EmployeeMapper.getActiveOfZepEmploymentPeriods(employmentPeriods);
        assertThat(active).isTrue();
    }
    @Test
    public void getActive_whenActive_withNullElement() {
        ZepEmploymentPeriod[] employmentPeriods = {
                ZepEmploymentPeriod.builder()
                        .endDate(null)
                        .build(),
                null,
                ZepEmploymentPeriod.builder()
                        .endDate(LocalDateTime.of(2018, 2, 1, 12, 32, 12))
                        .build()

        };

        boolean active = EmployeeMapper.getActiveOfZepEmploymentPeriods(employmentPeriods);
        assertThat(active).isTrue();
    }
    @Test
    public void getInactiveWhen_AllNull() {
        ZepEmploymentPeriod[] employmentPeriods = {
                null
        };

        boolean active = EmployeeMapper.getActiveOfZepEmploymentPeriods(employmentPeriods);
        assertThat(active).isFalse();
    }
}
