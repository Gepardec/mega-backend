package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.application.configuration.ZepConfig;
import com.gepardec.mega.hexagon.generated.model.WorkTimeReportDto;
import com.gepardec.mega.hexagon.shared.adapter.inbound.rest.SharedRefRestMapper;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEntry;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkTimeRestMapperTest {

    private static final String PROJECT_URL_PREFIX = "https://zep.example.test/project/";
    private static final String EMPLOYEE_URL_PREFIX = "https://zep.example.test/employee/";

    private final WorkTimeRestMapper mapper = createMapper();

    private WorkTimeRestMapper createMapper() {
        ZepConfig zepConfig = mock(ZepConfig.class);
        when(zepConfig.buildProjectUrl(anyInt()))
                .thenAnswer(invocation -> PROJECT_URL_PREFIX + invocation.getArgument(0, Integer.class));
        when(zepConfig.buildEmployeeUrl(any(ZepUsername.class)))
                .thenAnswer(invocation -> EMPLOYEE_URL_PREFIX + invocation.getArgument(0, ZepUsername.class).value());
        SharedRefRestMapper sharedRefRestMapper = Mappers.getMapper(SharedRefRestMapper.class);
        sharedRefRestMapper.setZepConfig(zepConfig);
        return new WorkTimeRestMapperImpl(sharedRefRestMapper);
    }

    @Test
    void toDto_shouldMapReportAndNestedReferences() {
        UserId employeeId = UserId.of(Instancio.create(UUID.class));
        ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
        WorkTimeReport report = new WorkTimeReport(
                YearMonth.of(2026, 3),
                List.of(new WorkTimeEntry(
                        new UserRef(employeeId, FullName.of("Ada", "Lovelace"), ZepUsername.of("ada")),
                        new ProjectRef(projectId, 77, "Spec First"),
                        12.5d,
                        1.5d,
                        20.0d
                ))
        );

        WorkTimeReportDto response = mapper.toDto(report);

        assertThat(response.getPayrollMonth()).isEqualTo("2026-03");
        assertThat(response.getEntries()).singleElement().satisfies(entry -> {
            assertThat(entry.getEmployee().getId()).isEqualTo(employeeId.value());
            assertThat(entry.getEmployee().getFullName()).isEqualTo("Ada Lovelace");
            assertThat(entry.getEmployee().getZepUrl()).isEqualTo(EMPLOYEE_URL_PREFIX + "ada");
            assertThat(entry.getProject().getId()).isEqualTo(projectId.value());
            assertThat(entry.getProject().getName()).isEqualTo("Spec First");
            assertThat(entry.getProject().getZepUrl()).isEqualTo(PROJECT_URL_PREFIX + "77");
            assertThat(entry.getBillableHours()).isEqualTo(12.5d);
            assertThat(entry.getNonBillableHours()).isEqualTo(1.5d);
            assertThat(entry.getEmployeeMonthTotalHours()).isEqualTo(20.0d);
        });
    }
}
