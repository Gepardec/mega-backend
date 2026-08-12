package com.gepardec.mega.hexagon.monthend.adapter.inbound;

import com.gepardec.mega.hexagon.monthend.adapter.outbound.MonthEndTaskRepositoryAdapter;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.project.adapter.outbound.ProjectRepositoryAdapter;
import com.gepardec.mega.hexagon.project.application.ProjectSettingsService;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.user.adapter.outbound.UserRepositoryAdapter;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestTransaction
class LeistungsnachweisEnabledTaskReopenerIT {

    @Inject
    ProjectSettingsService projectSettingsService;

    @Inject
    MonthEndTaskRepositoryAdapter monthEndTaskRepositoryAdapter;

    @Inject
    ProjectRepositoryAdapter projectRepositoryAdapter;

    @Inject
    UserRepositoryAdapter userRepositoryAdapter;

    @Test
    void reactivatingLeistungsnachweis_shouldReopenClosedTasksInDatabase() {
        YearMonth currentMonth = YearMonth.from(Clock.fixed(Instant.parse("2023-11-03T10:00:00Z"), ZoneOffset.UTC).instant().atZone(ZoneOffset.UTC));
        YearMonth previousMonth = currentMonth.minusMonths(1);

        User lead = user("lead", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User employee = user("employee", Set.of(Role.EMPLOYEE));
        userRepositoryAdapter.saveAll(List.of(lead, employee));

        Project project = project(true).withLeads(Set.of(lead.id()));
        projectRepositoryAdapter.saveAll(List.of(project));

        MonthEndTask closedTaskCurrent = closedLeistungsnachweisTask(currentMonth, project.id(), employee.id(), lead.id());
        MonthEndTask closedTaskPrevious = closedLeistungsnachweisTask(previousMonth, project.id(), employee.id(), lead.id());
        monthEndTaskRepositoryAdapter.saveAll(List.of(closedTaskCurrent, closedTaskPrevious));

        projectSettingsService.setLeistungsnachweisEnabled(project.id(), lead.id(), true);

        List<MonthEndTask> reopenedCurrent = monthEndTaskRepositoryAdapter.findByMonth(currentMonth).stream()
                .filter(task -> task.projectId().equals(project.id()))
                .filter(task -> task.type() == MonthEndTaskType.LEISTUNGSNACHWEIS)
                .toList();
        assertThat(reopenedCurrent).hasSize(1);
        assertThat(reopenedCurrent.getFirst().status()).isEqualTo(MonthEndTaskStatus.OPEN);

        List<MonthEndTask> reopenedPrevious = monthEndTaskRepositoryAdapter.findByMonth(previousMonth).stream()
                .filter(task -> task.projectId().equals(project.id()))
                .filter(task -> task.type() == MonthEndTaskType.LEISTUNGSNACHWEIS)
                .toList();
        assertThat(reopenedPrevious).hasSize(1);
        assertThat(reopenedPrevious.getFirst().status()).isEqualTo(MonthEndTaskStatus.OPEN);

        assertThat(monthEndTaskRepositoryAdapter.findClosedLeistungsnachweisTasks(currentMonth, project.id())).isEmpty();
        assertThat(monthEndTaskRepositoryAdapter.findClosedLeistungsnachweisTasks(previousMonth, project.id())).isEmpty();
    }

    private MonthEndTask closedLeistungsnachweisTask(YearMonth month, ProjectId projectId, UserId employeeId, UserId leadId) {
        return MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.LEISTUNGSNACHWEIS,
                projectId, employeeId, Set.of(leadId)
        ).closeBySystem();
    }

    private User user(String username, Set<Role> roles) {
        return new User(
                UserId.generate(),
                Email.of(username + "@example.com"),
                FullName.of("Test", "User"),
                ZepUsername.of(username),
                null,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2020, 1, 1), null)),
                roles
        );
    }

    private Project project(boolean billable) {
        return Project.create(
                ProjectId.generate(),
                new com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile(
                        42, "Project-42", LocalDate.of(2025, 1, 1), null, billable)
        );
    }
}
