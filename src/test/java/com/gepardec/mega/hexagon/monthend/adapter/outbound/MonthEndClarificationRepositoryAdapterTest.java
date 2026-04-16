package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.project.adapter.outbound.ProjectRepositoryAdapter;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.adapter.outbound.UserRepositoryAdapter;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.User;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestTransaction
class MonthEndClarificationRepositoryAdapterTest {

    @Inject
    MonthEndClarificationRepositoryAdapter clarificationRepositoryAdapter;

    @Inject
    UserRepositoryAdapter userRepositoryAdapter;

    @Inject
    ProjectRepositoryAdapter projectRepositoryAdapter;

    @Test
    void findOpenEmployeeClarifications_shouldReturnOnlyOpenClarificationsForSubjectEmployee() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("employee", Set.of(Role.EMPLOYEE));
        User lead = user("lead", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User otherEmployee = user("other-employee", Set.of(Role.EMPLOYEE));
        userRepositoryAdapter.saveAll(List.of(employee, lead, otherEmployee));

        Project project = project(42, true);
        projectRepositoryAdapter.saveAll(List.of(project));

        MonthEndClarification openClarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                project.id(),
                employee.id(),
                employee.id(),
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(lead.id()),
                "Please review this.",
                Instant.parse("2026-03-31T08:00:00Z")
        );
        MonthEndClarification doneClarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                project.id(),
                employee.id(),
                employee.id(),
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(lead.id()),
                "Already resolved",
                Instant.parse("2026-03-31T08:05:00Z")
        ).resolve(lead.id(), "Done", Instant.parse("2026-03-31T08:10:00Z"));
        MonthEndClarification otherEmployeeClarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                project.id(),
                otherEmployee.id(),
                otherEmployee.id(),
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(lead.id()),
                "Other employee clarification",
                Instant.parse("2026-03-31T08:15:00Z")
        );
        clarificationRepositoryAdapter.save(openClarification);
        clarificationRepositoryAdapter.save(doneClarification);
        clarificationRepositoryAdapter.save(otherEmployeeClarification);

        List<MonthEndClarification> clarifications = clarificationRepositoryAdapter.findOpenEmployeeClarifications(
                employee.id(),
                month
        );

        assertThat(clarifications).containsExactly(openClarification);
    }

    @Test
    void findAllEmployeeClarifications_shouldReturnOpenAndDoneClarificationsForSubjectEmployee() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("employee-all", Set.of(Role.EMPLOYEE));
        User lead = user("lead-all", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User otherEmployee = user("other-employee-all", Set.of(Role.EMPLOYEE));
        userRepositoryAdapter.saveAll(List.of(employee, lead, otherEmployee));

        Project project = project(52, true);
        projectRepositoryAdapter.saveAll(List.of(project));

        MonthEndClarification openClarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                project.id(),
                employee.id(),
                employee.id(),
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(lead.id()),
                "Please review this open clarification.",
                Instant.parse("2026-03-31T09:00:00Z")
        );
        MonthEndClarification doneClarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                project.id(),
                employee.id(),
                employee.id(),
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(lead.id()),
                "Already resolved clarification",
                Instant.parse("2026-03-31T09:05:00Z")
        ).resolve(lead.id(), "Handled", Instant.parse("2026-03-31T09:10:00Z"));
        MonthEndClarification otherMonthClarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month.plusMonths(1),
                project.id(),
                employee.id(),
                employee.id(),
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(lead.id()),
                "Other month clarification",
                Instant.parse("2026-04-01T09:00:00Z")
        );
        MonthEndClarification otherEmployeeClarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                project.id(),
                otherEmployee.id(),
                otherEmployee.id(),
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(lead.id()),
                "Other employee clarification",
                Instant.parse("2026-03-31T09:15:00Z")
        );
        clarificationRepositoryAdapter.save(openClarification);
        clarificationRepositoryAdapter.save(doneClarification);
        clarificationRepositoryAdapter.save(otherMonthClarification);
        clarificationRepositoryAdapter.save(otherEmployeeClarification);

        List<MonthEndClarification> clarifications = clarificationRepositoryAdapter.findAllEmployeeClarifications(
                employee.id(),
                month
        );

        assertThat(clarifications).containsExactlyInAnyOrder(openClarification, doneClarification);
    }

    @Test
    void findOpenProjectLeadClarifications_shouldReturnVisibleClarificationsAndHideDoneOnes() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("subject", Set.of(Role.EMPLOYEE));
        User leadA = user("leadA", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User leadB = user("leadB", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User leadC = user("leadC", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(employee, leadA, leadB, leadC));

        Project project = project(99, true);
        projectRepositoryAdapter.saveAll(List.of(project));

        MonthEndClarification sharedClarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                project.id(),
                employee.id(),
                leadA.id(),
                MonthEndClarificationSide.PROJECT_LEAD,
                Set.of(leadA.id(), leadB.id()),
                "Need employee follow-up",
                Instant.parse("2026-03-31T08:00:00Z")
        );
        MonthEndClarification doneClarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                project.id(),
                employee.id(),
                employee.id(),
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(leadA.id(), leadB.id()),
                "Resolved issue",
                Instant.parse("2026-03-31T08:05:00Z")
        ).resolve(leadA.id(), "Done", Instant.parse("2026-03-31T08:06:00Z"));
        clarificationRepositoryAdapter.save(sharedClarification);
        clarificationRepositoryAdapter.save(doneClarification);

        assertThat(clarificationRepositoryAdapter.findOpenProjectLeadClarifications(leadA.id(), month))
                .containsExactly(sharedClarification);
        assertThat(clarificationRepositoryAdapter.findOpenProjectLeadClarifications(leadB.id(), month))
                .containsExactly(sharedClarification);
        assertThat(clarificationRepositoryAdapter.findOpenProjectLeadClarifications(leadC.id(), month))
                .isEmpty();
    }

    @Test
    void findAllProjectLeadClarifications_shouldReturnOpenAndDoneClarificationsForLedProjects() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("subject-all", Set.of(Role.EMPLOYEE));
        User leadA = user("lead-all-a", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User leadB = user("lead-all-b", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User leadC = user("lead-all-c", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(employee, leadA, leadB, leadC));

        Project visibleProject = project(109, true);
        Project hiddenProject = project(110, true);
        projectRepositoryAdapter.saveAll(List.of(visibleProject, hiddenProject));

        MonthEndClarification openClarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                visibleProject.id(),
                employee.id(),
                leadA.id(),
                MonthEndClarificationSide.PROJECT_LEAD,
                Set.of(leadA.id(), leadB.id()),
                "Open clarification for led project",
                Instant.parse("2026-03-31T10:00:00Z")
        );
        MonthEndClarification doneClarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                visibleProject.id(),
                employee.id(),
                employee.id(),
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(leadA.id(), leadB.id()),
                "Done clarification for led project",
                Instant.parse("2026-03-31T10:05:00Z")
        ).resolve(leadA.id(), "Resolved", Instant.parse("2026-03-31T10:06:00Z"));
        MonthEndClarification otherMonthClarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month.plusMonths(1),
                visibleProject.id(),
                employee.id(),
                employee.id(),
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(leadA.id(), leadB.id()),
                "Other month clarification",
                Instant.parse("2026-04-01T10:00:00Z")
        );
        MonthEndClarification outOfScopeClarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                hiddenProject.id(),
                employee.id(),
                leadC.id(),
                MonthEndClarificationSide.PROJECT_LEAD,
                Set.of(leadC.id()),
                "Hidden clarification",
                Instant.parse("2026-03-31T10:07:00Z")
        );
        clarificationRepositoryAdapter.save(openClarification);
        clarificationRepositoryAdapter.save(doneClarification);
        clarificationRepositoryAdapter.save(otherMonthClarification);
        clarificationRepositoryAdapter.save(outOfScopeClarification);

        assertThat(clarificationRepositoryAdapter.findAllProjectLeadClarifications(leadA.id(), month))
                .containsExactlyInAnyOrder(openClarification, doneClarification);
        assertThat(clarificationRepositoryAdapter.findAllProjectLeadClarifications(leadB.id(), month))
                .containsExactlyInAnyOrder(openClarification, doneClarification);
        assertThat(clarificationRepositoryAdapter.findAllProjectLeadClarifications(leadC.id(), month))
                .containsExactly(outOfScopeClarification);
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

    private Project project(int zepId, boolean billable) {
        return Project.create(
                ProjectId.generate(),
                new ZepProjectProfile(zepId, "Project-" + zepId, LocalDate.of(2025, 1, 1), null, billable)
        );
    }
}
