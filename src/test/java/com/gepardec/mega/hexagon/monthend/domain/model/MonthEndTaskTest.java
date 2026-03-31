package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import org.assertj.core.api.ThrowableAssert;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonthEndTaskTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId employeeId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId leadA = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId leadB = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId outsider = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));

    @Test
    void create_shouldRejectProjectLeadReviewWithoutSubjectEmployee() {
        ThrowableAssert.ThrowingCallable throwingCallable = () -> MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                null,
                Set.of(leadA)
        );

        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("subject employee");
    }

    @Test
    void create_shouldRejectAbrechnungWithSubjectEmployee() {
        ThrowableAssert.ThrowingCallable throwingCallable = () -> MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.ABRECHNUNG,
                projectId,
                employeeId,
                Set.of(leadA)
        );

        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not reference a subject employee");
    }

    @Test
    void create_shouldDeriveCompletionPolicyFromTaskType() {
        MonthEndTask task = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                employeeId,
                Set.of(employeeId)
        );

        assertThat(task.completionPolicy()).isEqualTo(MonthEndCompletionPolicy.INDIVIDUAL_ACTOR);
    }

    @Test
    void create_shouldRejectEmployeeOwnedTaskWithMultipleEligibleActors() {
        ThrowableAssert.ThrowingCallable throwingCallable = () -> MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.LEISTUNGSNACHWEIS,
                projectId,
                employeeId,
                Set.of(employeeId, leadA)
        );

        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactly one eligible actor");
    }

    @Test
    void create_shouldRejectEmployeeOwnedTaskWithoutSubjectEmployee() {
        ThrowableAssert.ThrowingCallable throwingCallable = () -> MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                null,
                Set.of(employeeId)
        );

        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("require a subject employee");
    }

    @Test
    void create_shouldRejectEmployeeOwnedTaskWhenSubjectDoesNotMatchEligibleActor() {
        ThrowableAssert.ThrowingCallable throwingCallable = () -> MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                employeeId,
                Set.of(leadA)
        );

        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("eligible employee actor");
    }

    @Test
    void complete_shouldMarkTaskDoneAndStoreCompletingActor_whenActorEligible() {
        MonthEndTask task = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                employeeId,
                Set.of(leadA, leadB)
        );

        MonthEndTask completedTask = task.complete(leadA);

        assertThat(completedTask.status()).isEqualTo(MonthEndTaskStatus.DONE);
        assertThat(completedTask.completedBy()).isEqualTo(leadA);
    }

    @Test
    void complete_shouldRemainIdempotentAndKeepFirstCompleter_whenTaskAlreadyDone() {
        MonthEndTask task = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                employeeId,
                Set.of(leadA, leadB)
        );

        MonthEndTask firstCompletion = task.complete(leadA);
        MonthEndTask secondCompletion = firstCompletion.complete(leadB);

        assertThat(secondCompletion.status()).isEqualTo(MonthEndTaskStatus.DONE);
        assertThat(secondCompletion.completedBy()).isEqualTo(leadA);
    }

    @Test
    void complete_shouldRejectIneligibleActor() {
        MonthEndTask task = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                employeeId,
                Set.of(leadA, leadB)
        );

        assertThatThrownBy(() -> task.complete(outsider))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not eligible");
    }

    @Test
    void businessKey_shouldDifferentiateEmployeeOwnedTasksByEligibleActor() {
        MonthEndTask firstTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                employeeId,
                Set.of(employeeId)
        );
        MonthEndTask secondTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                leadA,
                Set.of(leadA)
        );

        assertThat(firstTask.businessKey()).isNotEqualTo(secondTask.businessKey());
    }
}
