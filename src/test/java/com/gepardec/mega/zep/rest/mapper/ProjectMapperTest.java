package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.BillabilityPreset;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.zep.rest.dto.ZepBillingType;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployeeType;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ProjectMapperTest {
    @Inject
    ProjectMapper projectMapper;

    @InjectMock
    Logger logger;

    @Test
    void fullZepProject_thenFullProject() {
        List<ZepProjectEmployee> zepProjectEmployees = List.of(
                ZepProjectEmployee.builder()
                        .username("Anton")
                        .type(ZepProjectEmployeeType.builder().id(1).build())
                        .build(),
                ZepProjectEmployee.builder()
                        .username("Berta")
                        .type(ZepProjectEmployeeType.builder().id(2).build())
                        .build(),
                ZepProjectEmployee.builder()
                        .username("Caesar")
                        .type(ZepProjectEmployeeType.builder().id(2).build())
                        .build()
        );


        int billabilityZepId = BillabilityPreset.BILLABLE.getZepId();

        ZepProject zepProject = ZepProject.builder()
                .id(1)
                .name("MEGA")
                .startDate(LocalDateTime.of(2020, 7, 8, 12, 32, 31))
                .endDate(LocalDateTime.of(2025, 2, 8, 12, 32, 31))
                .billingType(new ZepBillingType(billabilityZepId))
                .build();

        LocalDate startDate = zepProject.startDate() == null ?
                null : zepProject.startDate().toLocalDate();
        LocalDate endDate = zepProject.endDate() == null ?
                null : zepProject.endDate().toLocalDate();

        Project project = projectMapper.map(zepProject).build();
        assertThat(project.getZepId()).isEqualTo(zepProject.id());
        assertThat(project.getProjectId()).isEqualTo(zepProject.name());
        assertThat(project.getStartDate()).isEqualTo(startDate);
        assertThat(project.getEndDate()).isEqualTo(endDate);
        assertThat(project.getBillabilityPreset().getZepId()).isEqualTo(billabilityZepId);
    }

    @Test
    void map_whenZepProjectIsNull_thenReturnNullAndLogMessage() {
        assertThat(projectMapper.map(null)).isNull();
        Mockito.verify(logger).info("ZEP REST implementation -- While trying to map ZepProject to Project, ZepProject was null");
    }

    @Test
    void map_whenStartAndDateAreNull_thenReturnProjectWithCorrectStartAndEndDate() {
        int billabilityZepId = BillabilityPreset.BILLABLE.getZepId();

        ZepProject zepProject = ZepProject.builder()
                .id(1)
                .name("MEGA")
                .startDate(null)
                .endDate(null)
                .billingType(new ZepBillingType(billabilityZepId))
                .build();

        Project project = projectMapper.map(zepProject).build();
        assertThat(project.getStartDate()).isNull();
        assertThat(project.getEndDate()).isNotNull();
    }

    @Test
    void map_whenStartAndDateAreNullAndBillingTypeNotPresent_thenReturnProjectWithCorrectStartAndEndDate() {

        ZepProject zepProject = ZepProject.builder()
                .id(1)
                .name("MEGA")
                .startDate(null)
                .endDate(null)
                .billingType(new ZepBillingType(0))
                .build();

        Project project = projectMapper.map(zepProject).build();
        assertThat(project.getStartDate()).isNull();
        assertThat(project.getEndDate()).isNotNull();
        assertThat(project.getBillabilityPreset()).isNull();
    }
}
