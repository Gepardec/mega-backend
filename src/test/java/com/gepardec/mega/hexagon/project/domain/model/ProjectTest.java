package com.gepardec.mega.hexagon.project.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(project.getId()).isEqualTo(id);
        assertThat(project.getZepId()).isEqualTo(42);
        assertThat(project.getName()).isEqualTo("Alpha");
        assertThat(project.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(project.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    }

    @Test
    void create_leadsSetIsEmpty() {
        Project project = Project.create(ProjectId.generate(), profile(1, "Alpha"));

        assertThat(project.getLeads()).isEmpty();
    }

    @Test
    void reconstitute_setsAllFields() {
        ProjectId id = ProjectId.generate();
        UUID leadId = UUID.randomUUID();
        LocalDate start = LocalDate.of(2023, 6, 1);
        LocalDate end = LocalDate.of(2024, 6, 1);

        Project project = Project.reconstitute(id, 99, "Beta", start, end, false, Set.of(leadId));

        assertThat(project.getId()).isEqualTo(id);
        assertThat(project.getZepId()).isEqualTo(99);
        assertThat(project.getName()).isEqualTo("Beta");
        assertThat(project.getStartDate()).isEqualTo(start);
        assertThat(project.getEndDate()).isEqualTo(end);
        assertThat(project.getLeads()).containsExactly(leadId);
    }

    @Test
    void syncFromZep_updatesMutableFields() {
        Project project = Project.create(ProjectId.generate(), profile(10, "Old Name"));
        ZepProjectProfile updated = new ZepProjectProfile(10, "New Name", LocalDate.of(2025, 1, 1), null, false);

        project.syncFromZep(updated);

        assertThat(project.getName()).isEqualTo("New Name");
        assertThat(project.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(project.getEndDate()).isNull();
    }

    @Test
    void syncFromZep_doesNotChangeId() {
        ProjectId id = ProjectId.generate();
        Project project = Project.create(id, profile(10, "X"));

        project.syncFromZep(new ZepProjectProfile(10, "Y", LocalDate.now(), null, false));

        assertThat(project.getId()).isEqualTo(id);
        assertThat(project.getZepId()).isEqualTo(10);
    }

    @Test
    void setLeads_replacesLeadsSet() {
        Project project = Project.create(ProjectId.generate(), profile(5, "Gamma"));
        UUID leadA = UUID.randomUUID();
        UUID leadB = UUID.randomUUID();

        project.setLeads(Set.of(leadA, leadB));

        assertThat(project.getLeads()).containsExactlyInAnyOrder(leadA, leadB);
    }

    @Test
    void setLeads_replacesExistingLeads() {
        UUID oldLead = UUID.randomUUID();
        Project project = Project.reconstitute(ProjectId.generate(), 5, "Delta",
                LocalDate.now(), null, false, Set.of(oldLead));
        UUID newLead = UUID.randomUUID();

        project.setLeads(Set.of(newLead));

        assertThat(project.getLeads()).containsExactly(newLead);
        assertThat(project.getLeads()).doesNotContain(oldLead);
    }

    @Test
    void create_setBillableFromProfile() {
        Project billable = Project.create(ProjectId.generate(), billableProfile(1, "Billable"));
        Project notBillable = Project.create(ProjectId.generate(), profile(2, "Internal"));

        assertThat(billable.isBillable()).isTrue();
        assertThat(notBillable.isBillable()).isFalse();
    }

    @Test
    void reconstitute_setBillableFromParameter() {
        Project billable = Project.reconstitute(ProjectId.generate(), 1, "Billable",
                LocalDate.now(), null, true, Set.of());
        Project notBillable = Project.reconstitute(ProjectId.generate(), 2, "Internal",
                LocalDate.now(), null, false, Set.of());

        assertThat(billable.isBillable()).isTrue();
        assertThat(notBillable.isBillable()).isFalse();
    }

    @Test
    void syncFromZep_updatesBillable() {
        Project project = Project.create(ProjectId.generate(), profile(10, "Project"));
        assertThat(project.isBillable()).isFalse();

        project.syncFromZep(billableProfile(10, "Project"));

        assertThat(project.isBillable()).isTrue();
    }

    @Test
    void leads_returnsDefensiveCopy() {
        Project project = Project.create(ProjectId.generate(), profile(1, "X"));
        project.setLeads(Set.of(UUID.randomUUID()));

        Set<UUID> leads = project.getLeads();
        assertThat(leads).isUnmodifiable();
    }
}
