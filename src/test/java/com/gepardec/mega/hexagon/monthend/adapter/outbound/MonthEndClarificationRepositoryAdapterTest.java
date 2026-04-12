package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.project.adapter.outbound.ProjectRepositoryAdapter;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile;
import com.gepardec.mega.hexagon.user.adapter.outbound.UserRepositoryAdapter;
import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.FullName;
import com.gepardec.mega.hexagon.user.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.ZepUsername;
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
