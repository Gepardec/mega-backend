package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.BillabilityPreset;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.zep.rest.entity.ZepProject;
import com.gepardec.mega.zep.rest.entity.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.entity.ZepProjectEmployeeTypeBuilder;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class ProjectMapperTest {
    @Test
    public void fullZepProject_thenFullProject() {
        ProjectMapper projectMapper = new ProjectMapper();
        List<ZepProjectEmployee> zepProjectEmployees = List.of(
                ZepProjectEmployee.builder()
                        .username("Anton")
                        .lead(true)
                        .type(new ZepProjectEmployeeTypeBuilder().id(1).name("pm").build())
                        .build(),
                ZepProjectEmployee.builder()
                        .username("Berta")
                        .lead(false)
                        .type(new ZepProjectEmployeeTypeBuilder().id(2).name("engineer").build())
                        .build(),
                ZepProjectEmployee.builder()
                        .username("Caesar")
                        .lead(false)
                        .type(new ZepProjectEmployeeTypeBuilder().id(2).name("engineer").build())
                        .build()
        );


        int billabilityZepId = BillabilityPreset.BILLABLE.getZepId();

        ZepProject zepProject = ZepProject.builder()
                .id(1)
                .name("MEGA")
                .description("Make End of month Great Again")
                .startDate(LocalDateTime.of(2020, 7, 8, 12, 32, 31))
                .endDate(LocalDateTime.of(2025, 2, 8, 12, 32, 31))
                .status("active")
                .comments("MEGA Teams: teams.com/mega")
                .costObject(null)
                .costObjectIdentifier(null)
                .created(LocalDateTime.of(2020, 7, 8, 12, 32, 31))
                .modified(LocalDateTime.of(2020, 9, 12, 1, 18, 51))
                .keywords(List.of("intern"))
                .referenceOrder(null)
                .referenceCommission(null)
                .referenceProcurement(null)
                .referenceObject(null)
                .language("German")
                .currency("EUR")
                .url("https://mega.gepardec.com")
                .locationAddress("Sandleitengasse 872/189")
                .locationCity("Vienna")
                .locationState("Vienna")
                .locationCountry("Austria")
                .revenueAccount(null)
                .customerId("001")
                .customerContactId(1)
                .customerProjectReference("B187")
                .customerBillingAddressId(1)
                .customerShippingAddressId(1)
                .hasMultipleCustomers(null)
                .departmentId(1)
                .billingType(billabilityZepId)
                .billingTasks(2)
                .planHours("12")
                .planHoursPerDay("1")
                .planCanExceed(true)
                .planWarningPercent(80.0)
                .planWarningPercent2(60.0)
                .planWarningPercent3(40.0)
                .planWage(12.5)
                .planExpenses("expenses")
                .planHoursDone(1034.2)
                .planHoursInvoiced(80.5)
                .tasksCount(4)
                .employeesCount(4)
                .activitiesCount(0)
                .build();

        LocalDate startDate = zepProject.getStartDate() == null ?
                null : zepProject.getStartDate().toLocalDate();
        LocalDate endDate = zepProject.getEndDate() == null ?
                null : zepProject.getEndDate().toLocalDate();

        Project project = projectMapper.map(zepProject);
        assertThat(project.getZepId()).isEqualTo(zepProject.getId());
        assertThat(project.getProjectId()).isEqualTo(zepProject.getName());
        assertThat(project.getStartDate()).isEqualTo(startDate);
        assertThat(project.getEndDate()).isEqualTo(endDate);
        assertThat(project.getBillabilityPreset().getZepId()).isEqualTo(billabilityZepId);
    }
}
