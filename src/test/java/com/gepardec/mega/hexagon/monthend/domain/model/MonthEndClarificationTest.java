package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndActorNotAuthorizedException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndClarificationClosedException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndValidationException;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
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
                Set.of(leadA, leadB),
                "Please verify the missing entry.",
                createdAt
        );

        assertThat(clarification.status()).isEqualTo(MonthEndClarificationStatus.OPEN);
        assertThat(clarification.createdBy()).isEqualTo(employeeId);
        assertThat(clarification.subjectEmployeeId()).isEqualTo(employeeId);
        assertThat(clarification.createdAt()).isEqualTo(createdAt);
        assertThat(clarification.lastModifiedAt()).isEqualTo(createdAt);
        assertThat(clarification.resolvedBy()).isNull();
        assertThat(clarification.resolutionNote()).isNull();
    }

    @Test
    void create_shouldInitializeOpenClarification_whenLeadCreatesForEmployee() {
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                employeeId,
                leadA,
                Set.of(leadA, leadB),
                "Please provide supporting evidence.",
                createdAt
        );

        assertThat(clarification.status()).isEqualTo(MonthEndClarificationStatus.OPEN);
        assertThat(clarification.createdBy()).isEqualTo(leadA);
        assertThat(clarification.subjectEmployeeId()).isEqualTo(employeeId);
    }

    @Test
    void create_shouldInitializeProjectLevelClarification_whenLeadCreatesWithNoSubjectEmployee() {
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                null,
                leadA,
                Set.of(leadA, leadB),
                "Cross-lead discussion required.",
                createdAt
        );

        assertThat(clarification.status()).isEqualTo(MonthEndClarificationStatus.OPEN);
        assertThat(clarification.subjectEmployeeId()).isNull();
        assertThat(clarification.createdBy()).isEqualTo(leadA);
    }

    @Test
    void create_shouldThrow_whenProjectLevelClarificationCreatedByNonLead() {
        ThrowableAssert.ThrowingCallable throwingCallable = () -> MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                null,
                employeeId,
                Set.of(leadA, leadB),
                "This should fail.",
                createdAt
        );

        assertThatThrownBy(throwingCallable).isInstanceOf(MonthEndValidationException.class);
    }

    @Test
    void editText_shouldUpdateTextAndLastModifiedAt_whenEditedByCreator() {
        MonthEndClarification clarification = employeeCreatedClarification();
        Instant modifiedAt = createdAt.plusSeconds(60);

        MonthEndClarification updated = clarification.editText(employeeId, "Updated follow-up text", modifiedAt);

        assertThat(updated.text()).isEqualTo("Updated follow-up text");
        assertThat(updated.lastModifiedAt()).isEqualTo(modifiedAt);
    }

    @Test
    void editText_shouldThrow_whenNonCreatorLeadAttemptsEdit() {
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                employeeId,
                leadA,
                Set.of(leadA, leadB),
                "Need updated project note",
                createdAt
        );

        ThrowableAssert.ThrowingCallable throwingCallable = () -> clarification.editText(leadB, "Edited by non-creator", createdAt.plusSeconds(120));

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(MonthEndActorNotAuthorizedException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void editText_shouldThrow_whenNonCreatorEmployeeAttemptsEdit() {
        MonthEndClarification clarification = employeeCreatedClarification();

        ThrowableAssert.ThrowingCallable throwingCallable = () -> clarification.editText(leadA, "Trying to edit", createdAt.plusSeconds(1));

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(MonthEndActorNotAuthorizedException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void resolve_shouldStoreResolverAndOptionalResolutionNote_whenResolvedByLead() {
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
    void resolve_shouldThrow_whenCreatorAttemptsToResolveOwnClarification() {
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                employeeId,
                leadA,
                Set.of(leadA, leadB),
                "Please adjust your note",
                createdAt
        );

        ThrowableAssert.ThrowingCallable throwingCallable = () -> clarification.resolve(leadA, null, createdAt.plusSeconds(1));

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(MonthEndActorNotAuthorizedException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void resolve_shouldAllowCrossLeadResolution_whenLeadBResolvesLeadACreatedClarification() {
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                null,
                leadA,
                Set.of(leadA, leadB),
                "Cross-lead discussion.",
                createdAt
        );

        MonthEndClarification resolved = clarification.resolve(leadB, "Acknowledged.", createdAt.plusSeconds(60));

        assertThat(resolved.status()).isEqualTo(MonthEndClarificationStatus.DONE);
        assertThat(resolved.resolvedBy()).isEqualTo(leadB);
    }

    @Test
    void editText_shouldThrow_whenClarificationAlreadyDone() {
        MonthEndClarification clarification = employeeCreatedClarification()
                .resolve(leadA, "Done", createdAt.plusSeconds(10));

        ThrowableAssert.ThrowingCallable throwingCallable = () -> clarification.editText(employeeId, "No longer editable", createdAt.plusSeconds(20));

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(MonthEndClarificationClosedException.class)
                .hasMessageContaining("cannot be edited");
    }

    @Test
    void canBeResolvedBy_shouldReturnFalse_whenClarificationAlreadyDone() {
        MonthEndClarification clarification = employeeCreatedClarification()
                .resolve(leadA, "Done", createdAt.plusSeconds(10));

        assertThat(clarification.canBeResolvedBy(leadA)).isFalse();
        assertThat(clarification.canBeResolvedBy(leadB)).isFalse();
    }

    @Test
    void canDelete_shouldReturnTrue_whenActorIsCreatorAndClarificationIsOpen() {
        MonthEndClarification clarification = employeeCreatedClarification();

        assertThat(clarification.canDelete(employeeId)).isTrue();
    }

    @Test
    void canDelete_shouldReturnFalse_whenActorIsNotCreator() {
        MonthEndClarification clarification = employeeCreatedClarification();

        assertThat(clarification.canDelete(leadA)).isFalse();
    }

    @Test
    void canDelete_shouldReturnFalse_whenClarificationIsDone() {
        MonthEndClarification clarification = employeeCreatedClarification()
                .resolve(leadA, "Done", createdAt.plusSeconds(10));

        assertThat(clarification.canDelete(employeeId)).isFalse();
    }

    private MonthEndClarification employeeCreatedClarification() {
        return MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                employeeId,
                employeeId,
                Set.of(leadA, leadB),
                "Please verify the missing entry.",
                createdAt
        );
    }
}
