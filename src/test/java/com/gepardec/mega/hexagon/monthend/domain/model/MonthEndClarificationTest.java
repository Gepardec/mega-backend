package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import org.assertj.core.api.ThrowableAssert;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonthEndClarificationTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final UserId employeeId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadA = UserId.of(Instancio.create(UUID.class));
    private final UserId leadB = UserId.of(Instancio.create(UUID.class));
    private final Instant createdAt = Instant.parse("2026-03-31T08:00:00Z");

    @Test
    void create_shouldInitializeOpenClarification_whenCreatedByEmployee() {
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                employeeId,
                employeeId,
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(leadA, leadB),
                "Please verify the missing entry.",
                createdAt
        );

        assertThat(clarification.status()).isEqualTo(MonthEndClarificationStatus.OPEN);
        assertThat(clarification.createdBy()).isEqualTo(employeeId);
        assertThat(clarification.createdAt()).isEqualTo(createdAt);
        assertThat(clarification.lastModifiedAt()).isEqualTo(createdAt);
        assertThat(clarification.resolvedBy()).isNull();
        assertThat(clarification.resolutionNote()).isNull();
    }

    @Test
    void editText_shouldUpdateTextAndLastModifiedAt_whenEditedByEmployeeCreator() {
        MonthEndClarification clarification = employeeCreatedClarification();
        Instant modifiedAt = createdAt.plusSeconds(60);

        MonthEndClarification updated = clarification.editText(employeeId, "Updated follow-up text", modifiedAt);

        assertThat(updated.text()).isEqualTo("Updated follow-up text");
        assertThat(updated.lastModifiedAt()).isEqualTo(modifiedAt);
    }

    @Test
    void editText_shouldAllowEligibleLead_whenCreatedByLeadSide() {
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                employeeId,
                leadA,
                MonthEndClarificationSide.PROJECT_LEAD,
                Set.of(leadA, leadB),
                "Need updated project note",
                createdAt
        );
        Instant modifiedAt = createdAt.plusSeconds(120);

        MonthEndClarification updated = clarification.editText(leadB, "Need updated project note ASAP", modifiedAt);

        assertThat(updated.text()).isEqualTo("Need updated project note ASAP");
        assertThat(updated.lastModifiedAt()).isEqualTo(modifiedAt);
    }

    @Test
    void editText_shouldThrow_whenResolverSideAttemptsEdit() {
        MonthEndClarification clarification = employeeCreatedClarification();

        ThrowableAssert.ThrowingCallable throwingCallable = () -> clarification.editText(leadA, "Trying to edit", createdAt.plusSeconds(1));

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void resolve_shouldStoreResolverAndOptionalResolutionNote_whenResolvedByOppositeSide() {
        MonthEndClarification clarification = employeeCreatedClarification();
        Instant resolvedAt = createdAt.plusSeconds(300);

        MonthEndClarification resolved = clarification.resolve(leadA, "Checked and fixed.", resolvedAt);

        assertThat(resolved.status()).isEqualTo(MonthEndClarificationStatus.DONE);
        assertThat(resolved.resolvedBy()).isEqualTo(leadA);
        assertThat(resolved.resolutionNote()).isEqualTo("Checked and fixed.");
        assertThat(resolved.resolvedAt()).isEqualTo(resolvedAt);
        assertThat(resolved.lastModifiedAt()).isEqualTo(resolvedAt);
    }

    @Test
    void resolve_shouldThrow_whenActorIsNotAllowed() {
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                employeeId,
                leadA,
                MonthEndClarificationSide.PROJECT_LEAD,
                Set.of(leadA, leadB),
                "Please adjust your note",
                createdAt
        );

        ThrowableAssert.ThrowingCallable throwingCallable = () -> clarification.resolve(leadA, null, createdAt.plusSeconds(1));

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void editText_shouldThrow_whenClarificationAlreadyDone() {
        MonthEndClarification clarification = employeeCreatedClarification()
                .resolve(leadA, "Done", createdAt.plusSeconds(10));

        ThrowableAssert.ThrowingCallable throwingCallable = () -> clarification.editText(employeeId, "No longer editable", createdAt.plusSeconds(20));

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be edited");
    }

    private MonthEndClarification employeeCreatedClarification() {
        return MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                employeeId,
                employeeId,
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(leadA, leadB),
                "Please verify the missing entry.",
                createdAt
        );
    }
}
