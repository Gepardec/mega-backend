package com.gepardec.mega.hexagon.project.domain.model;

import com.gepardec.mega.hexagon.project.domain.error.LeistungsnachweisNotApplicableException;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectTest {

    private ZepProjectProfile profile(int zepId, String name) {
        return new ZepProjectProfile(zepId, name, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), false);
    }

    private ZepProjectProfile billableProfile(int zepId, String name) {
        return new ZepProjectProfile(zepId, name, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), true);
    }

    @Test
    void create_populatesFieldsFromProfile() {
        ProjectId id = ProjectId.generate();
        ZepProjectProfile profile = profile(42, "Alpha");

        Project project = Project.create(id, profile);

        assertThat(project.id()).isEqualTo(id);
        assertThat(project.zepId()).isEqualTo(42);
        assertThat(project.name()).isEqualTo("Alpha");
        assertThat(project.startDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(project.endDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    }

    @Test
    void create_leadsSetIsEmpty() {
        Project project = Project.create(ProjectId.generate(), profile(1, "Alpha"));

        assertThat(project.leads()).isEmpty();
    }

    @Test
    void constructor_setsAllFields() {
        ProjectId id = ProjectId.generate();
        UserId leadId = UserId.of(UUID.randomUUID());
        LocalDate start = LocalDate.of(2023, 6, 1);
        LocalDate end = LocalDate.of(2024, 6, 1);

        Project project = new Project(id, 99, "Beta", start, end, true, true, Set.of(leadId));

        assertThat(project.id()).isEqualTo(id);
        assertThat(project.zepId()).isEqualTo(99);
        assertThat(project.name()).isEqualTo("Beta");
        assertThat(project.startDate()).isEqualTo(start);
        assertThat(project.endDate()).isEqualTo(end);
        assertThat(project.leads()).containsExactly(leadId);
    }

    @Test
    void withSyncedZepData_returnsUpdatedProjectWithoutMutatingOriginal() {
        Project project = Project.create(ProjectId.generate(), profile(10, "Old Name"));
        ZepProjectProfile updated = new ZepProjectProfile(10, "New Name", LocalDate.of(2025, 1, 1), null, false);

        Project synchronizedProject = project.withSyncedZepData(updated);

        assertThat(synchronizedProject.name()).isEqualTo("New Name");
        assertThat(synchronizedProject.startDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(synchronizedProject.endDate()).isNull();
        assertThat(project.name()).isEqualTo("Old Name");
    }

    @Test
    void withSyncedZepData_doesNotChangeId() {
        ProjectId id = ProjectId.generate();
        Project project = Project.create(id, profile(10, "X"));

        Project synchronizedProject = project.withSyncedZepData(new ZepProjectProfile(10, "Y", LocalDate.now(), null, false));

        assertThat(synchronizedProject.id()).isEqualTo(id);
        assertThat(synchronizedProject.zepId()).isEqualTo(10);
    }

    @Test
    void withLeads_replacesLeadsSet() {
        Project project = Project.create(ProjectId.generate(), profile(5, "Gamma"));
        UserId leadA = UserId.of(UUID.randomUUID());
        UserId leadB = UserId.of(UUID.randomUUID());

        Project updatedProject = project.withLeads(Set.of(leadA, leadB));

        assertThat(updatedProject.leads()).containsExactlyInAnyOrder(leadA, leadB);
        assertThat(project.leads()).isEmpty();
    }

    @Test
    void withLeads_replacesExistingLeads() {
        UserId oldLead = UserId.of(UUID.randomUUID());
        Project project = new Project(ProjectId.generate(), 5, "Delta",
                LocalDate.now(), null, true, true, Set.of(oldLead));
        UserId newLead = UserId.of(UUID.randomUUID());

        Project updatedProject = project.withLeads(Set.of(newLead));

        assertThat(updatedProject.leads()).containsExactly(newLead);
        assertThat(updatedProject.leads()).doesNotContain(oldLead);
    }

    @Test
    void create_setBillableFromProfile() {
        Project billable = Project.create(ProjectId.generate(), billableProfile(1, "Billable"));
        Project notBillable = Project.create(ProjectId.generate(), profile(2, "Internal"));

        assertThat(billable.billable()).isTrue();
        assertThat(notBillable.billable()).isFalse();
    }

    @Test
    void constructor_setBillableFromParameter() {
        Project billable = new Project(ProjectId.generate(), 1, "Billable",
                LocalDate.now(), null, true, true, Set.of());
        Project notBillable = new Project(ProjectId.generate(), 2, "Internal",
                LocalDate.now(), null, false, false, Set.of());

        assertThat(billable.billable()).isTrue();
        assertThat(notBillable.billable()).isFalse();
    }

    @Test
    void withSyncedZepData_updatesBillable() {
        Project project = Project.create(ProjectId.generate(), profile(10, "Project"));
        assertThat(project.billable()).isFalse();

        Project synchronizedProject = project.withSyncedZepData(billableProfile(10, "Project"));

        assertThat(synchronizedProject.billable()).isTrue();
    }

    @Test
    void withLeads_preservesLeistungsnachweisEnabled() {
        Project project = new Project(ProjectId.generate(), 1, "X", LocalDate.now(), null, false, false, Set.of());

        Project updated = project.withLeads(Set.of(UserId.of(UUID.randomUUID())));
        assertThat(updated.leistungsnachweisEnabled()).isFalse();
    }

    @Test
    void leads_returnsDefensiveCopy() {
        Project project = Project.create(ProjectId.generate(), profile(1, "X"));
        project = project.withLeads(Set.of(UserId.of(UUID.randomUUID())));

        Set<UserId> leads = project.leads();
        assertThat(leads).isUnmodifiable();
    }

    @Test
    void isActiveIn_shouldTreatAnyOverlapWithMonthAsActive() {
        Project project = new Project(
                ProjectId.generate(),
                5,
                "Delta",
                LocalDate.of(2024, 3, 15),
                LocalDate.of(2024, 4, 15),
                false,
                false,
                Set.of()
        );

        assertThat(project.isActiveIn(YearMonth.of(2024, 2))).isFalse();
        assertThat(project.isActiveIn(YearMonth.of(2024, 3))).isTrue();
        assertThat(project.isActiveIn(YearMonth.of(2024, 4))).isTrue();
        assertThat(project.isActiveIn(YearMonth.of(2024, 5))).isFalse();
    }

    // --- 1.2 constructor guard / create() ---

    @Test
    void create_billableProfile_createsProjectWithFlagEnabled() {
        Project project = Project.create(ProjectId.generate(), billableProfile(1, "X"));

        assertThat(project.leistungsnachweisEnabled()).isTrue();
    }

    @Test
    void create_nonBillableProfile_createsProjectWithFlagDisabled() {
        Project project = Project.create(ProjectId.generate(), profile(1, "X"));

        assertThat(project.leistungsnachweisEnabled()).isFalse();
    }

    @Test
    void constructor_nonBillableWithFlagEnabled_throws() {
        ThrowableAssert.ThrowingCallable throwingCallable = () -> new Project(ProjectId.generate(), 1, "X", LocalDate.now(), null, false, true, Set.of());

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- 1.3 withSyncedZepData transition table ---

    @Test
    void withSyncedZepData_staysBillable_keepsFlag() {
        Project project = new Project(ProjectId.generate(), 1, "X", LocalDate.now(), null, true, false, Set.of());

        Project synced = project.withSyncedZepData(billableProfile(1, "X"));

        assertThat(synced.leistungsnachweisEnabled()).isFalse();
    }

    @Test
    void withSyncedZepData_leadOptOutOnBillableProject_survivesBillableResync() {
        Project project = Project.create(ProjectId.generate(), billableProfile(1, "X"))
                .withLeistungsnachweisEnabled(false);

        Project synced = project.withSyncedZepData(billableProfile(1, "X Updated"));

        assertThat(synced.leistungsnachweisEnabled()).isFalse();
    }

    @Test
    void withSyncedZepData_becomesNonBillable_disablesFlag() {
        Project project = Project.create(ProjectId.generate(), billableProfile(1, "X"));
        assertThat(project.leistungsnachweisEnabled()).isTrue();

        Project synced = project.withSyncedZepData(profile(1, "X"));

        assertThat(synced.leistungsnachweisEnabled()).isFalse();
    }

    @Test
    void withSyncedZepData_becomesBillableAgain_enablesFlag() {
        Project project = Project.create(ProjectId.generate(), profile(1, "X"));
        assertThat(project.leistungsnachweisEnabled()).isFalse();

        Project synced = project.withSyncedZepData(billableProfile(1, "X"));

        assertThat(synced.leistungsnachweisEnabled()).isTrue();
    }

    @Test
    void withSyncedZepData_staysNonBillable_keepsFlagDisabled() {
        Project project = Project.create(ProjectId.generate(), profile(1, "X"));

        Project synced = project.withSyncedZepData(profile(1, "X Updated"));

        assertThat(synced.leistungsnachweisEnabled()).isFalse();
    }

    // --- 1.4 withLeistungsnachweisEnabled ---

    @Test
    void withLeistungsnachweisEnabled_returnsNewInstanceWithToggledFlag() {
        Project project = Project.create(ProjectId.generate(), billableProfile(1, "X"));

        Project disabled = project.withLeistungsnachweisEnabled(false);
        Project reEnabled = disabled.withLeistungsnachweisEnabled(true);

        assertThat(disabled.leistungsnachweisEnabled()).isFalse();
        assertThat(reEnabled.leistungsnachweisEnabled()).isTrue();
        assertThat(project.leistungsnachweisEnabled()).isTrue();
    }

    @Test
    void withLeistungsnachweisEnabled_enableOnNonBillable_throws() {
        Project project = Project.create(ProjectId.generate(), profile(1, "X"));

        assertThatThrownBy(() -> project.withLeistungsnachweisEnabled(true))
                .isInstanceOf(LeistungsnachweisNotApplicableException.class);
    }

    @Test
    void withLeistungsnachweisEnabled_disableOnNonBillable_flagStaysFalse() {
        Project project = Project.create(ProjectId.generate(), profile(1, "X"));

        Project updated = project.withLeistungsnachweisEnabled(false);

        assertThat(updated.leistungsnachweisEnabled()).isFalse();
    }

    // --- 1.5 isLedBy / isLeistungsnachweisConfigurableFrom ---

    @Test
    void isLedBy_leadAndNonLead() {
        UserId leadId = UserId.of(UUID.randomUUID());
        UserId otherId = UserId.of(UUID.randomUUID());
        Project project = Project.create(ProjectId.generate(), billableProfile(1, "X")).withLeads(Set.of(leadId));

        assertThat(project.isLedBy(leadId)).isTrue();
        assertThat(project.isLedBy(otherId)).isFalse();
    }

    @Test
    void isLeistungsnachweisConfigurableFrom_nonBillable_isFalse() {
        Project project = Project.create(ProjectId.generate(), profile(1, "X"));

        assertThat(project.isLeistungsnachweisConfigurableFrom(YearMonth.of(2026, 9))).isFalse();
    }

    @Test
    void isLeistungsnachweisConfigurableFrom_endedBeforeMonth_isFalse() {
        Project project = new Project(ProjectId.generate(), 1, "X",
                LocalDate.of(2024, 1, 1), LocalDate.of(2026, 8, 31), true, true, Set.of());

        assertThat(project.isLeistungsnachweisConfigurableFrom(YearMonth.of(2026, 9))).isFalse();
    }

    @Test
    void isLeistungsnachweisConfigurableFrom_endsWithinMonth_isTrue() {
        Project project = new Project(ProjectId.generate(), 1, "X",
                LocalDate.of(2024, 1, 1), LocalDate.of(2026, 9, 10), true, true, Set.of());

        assertThat(project.isLeistungsnachweisConfigurableFrom(YearMonth.of(2026, 9))).isTrue();
    }

    @Test
    void isLeistungsnachweisConfigurableFrom_noEndDate_isTrue() {
        Project project = new Project(ProjectId.generate(), 1, "X",
                LocalDate.of(2024, 1, 1), null, true, true, Set.of());

        assertThat(project.isLeistungsnachweisConfigurableFrom(YearMonth.of(2026, 9))).isTrue();
    }

    @Test
    void isLeistungsnachweisConfigurableFrom_startsInFutureMonth_isTrue() {
        Project project = new Project(ProjectId.generate(), 1, "X",
                LocalDate.of(2026, 11, 1), null, true, true, Set.of());

        assertThat(project.isLeistungsnachweisConfigurableFrom(YearMonth.of(2026, 9))).isTrue();
    }
}
