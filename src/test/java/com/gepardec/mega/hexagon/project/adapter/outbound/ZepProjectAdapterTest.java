package com.gepardec.mega.hexagon.project.adapter.outbound;

import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployeeType;
import com.gepardec.mega.zep.rest.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZepProjectAdapterTest {

    @Mock
    ProjectService projectService;

    @Spy
    ZepProjectMapper mapper = Mappers.getMapper(ZepProjectMapper.class);

    @InjectMocks
    ZepProjectAdapter adapter;

    private ZepProject zepProject(int id, String name) {
        return ZepProject.builder()
                .id(id)
                .name(name)
                .startDate(LocalDateTime.of(2024, 1, 1, 0, 0))
                .endDate(LocalDateTime.of(2024, 12, 31, 0, 0))
                .build();
    }

    private ZepProjectEmployee employee(String username, int typeId) {
        ZepProjectEmployeeType type = typeId == 0 ? null
                : ZepProjectEmployeeType.builder().id(typeId).build();
        return ZepProjectEmployee.builder()
                .username(username)
                .type(type)
                .build();
    }

    @Test
    void fetchAll_delegatesToProjectService() {
        when(projectService.getAllProjects()).thenReturn(List.of(zepProject(1, "Alpha")));

        var profiles = adapter.fetchAll();

        assertThat(profiles).hasSize(1);
        assertThat(profiles.getFirst().zepId()).isEqualTo(1);
        assertThat(profiles.getFirst().name()).isEqualTo("Alpha");
    }

    @Test
    void fetchAll_mapsDateFieldsCorrectly() {
        when(projectService.getAllProjects()).thenReturn(List.of(zepProject(2, "Beta")));

        var profiles = adapter.fetchAll();

        assertThat(profiles.getFirst().startDate()).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0).toLocalDate());
        assertThat(profiles.getFirst().endDate()).isEqualTo(LocalDateTime.of(2024, 12, 31, 0, 0).toLocalDate());
    }

    @Test
    void fetchAll_handlesNullDates() {
        ZepProject noEnd = ZepProject.builder().id(3).name("NoEnd").startDate(LocalDateTime.now()).build();
        when(projectService.getAllProjects()).thenReturn(List.of(noEnd));

        var profiles = adapter.fetchAll();

        assertThat(profiles.getFirst().endDate()).isNull();
    }

    @Test
    void fetchLeadUsernames_filtersLeadsByTypeIdNonZero() {
        List<ZepProjectEmployee> employees = List.of(
                employee("lead1", 1),
                employee("regular", 0),
                employee("lead2", 2)
        );
        when(projectService.getProjectEmployeesForId(42, null)).thenReturn(employees);

        List<String> leads = adapter.fetchLeadUsernames(42);

        assertThat(leads).containsExactlyInAnyOrder("lead1", "lead2");
        assertThat(leads).doesNotContain("regular");
    }

    @Test
    void fetchLeadUsernames_excludesEmployeesWithNullType() {
        ZepProjectEmployee noType = ZepProjectEmployee.builder().username("noteam").type(null).build();
        when(projectService.getProjectEmployeesForId(10, null)).thenReturn(List.of(noType));

        List<String> leads = adapter.fetchLeadUsernames(10);

        assertThat(leads).isEmpty();
    }

    @Test
    void fetchLeadUsernames_returnsEmptyWhenNoLeads() {
        when(projectService.getProjectEmployeesForId(99, null)).thenReturn(List.of());

        List<String> leads = adapter.fetchLeadUsernames(99);

        assertThat(leads).isEmpty();
    }
}
