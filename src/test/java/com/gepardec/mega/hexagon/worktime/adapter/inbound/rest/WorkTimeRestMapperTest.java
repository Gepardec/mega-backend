package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.WorkTimeReportResponse;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEmployee;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEntry;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeProject;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkTimeRestMapperTest {

    private final WorkTimeRestMapper mapper = Mappers.getMapper(WorkTimeRestMapper.class);

    @Test
    void toResponse_shouldMapReportAndNestedReferences() {
        UserId employeeId = UserId.of(Instancio.create(UUID.class));
        ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
        WorkTimeReport report = new WorkTimeReport(
                YearMonth.of(2026, 3),
                List.of(new WorkTimeEntry(
                        new WorkTimeEmployee(employeeId, "Ada Lovelace"),
                        new WorkTimeProject(projectId, "Spec First"),
                        12.5d,
                        1.5d,
                        20.0d
                ))
        );

        WorkTimeReportResponse response = mapper.toResponse(report);

        assertThat(response.getPayrollMonth()).isEqualTo("2026-03");
        assertThat(response.getEntries()).singleElement().satisfies(entry -> {
            assertThat(entry.getEmployee().getId()).isEqualTo(employeeId.value());
            assertThat(entry.getEmployee().getName()).isEqualTo("Ada Lovelace");
            assertThat(entry.getProject().getId()).isEqualTo(projectId.value());
            assertThat(entry.getProject().getName()).isEqualTo("Spec First");
            assertThat(entry.getBillableHours()).isEqualTo(12.5d);
            assertThat(entry.getNonBillableHours()).isEqualTo(1.5d);
            assertThat(entry.getEmployeeMonthTotalHours()).isEqualTo(20.0d);
        });
    }
}
