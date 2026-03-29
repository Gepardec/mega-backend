package com.gepardec.mega.hexagon.project.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectTest {

    private ZepProjectProfile profile(int zepId, String name) {
        return new ZepProjectProfile(zepId, name, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
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
    void reconstitute_setsAllFields() {
        ProjectId id = ProjectId.generate();
        UUID leadId = UUID.randomUUID();
        LocalDate start = LocalDate.of(2023, 6, 1);
        LocalDate end = LocalDate.of(2024, 6, 1);

        Project project = Project.reconstitute(id, 99, "Beta", start, end, Set.of(leadId));

        assertThat(project.id()).isEqualTo(id);
        assertThat(project.zepId()).isEqualTo(99);
        assertThat(project.name()).isEqualTo("Beta");
        assertThat(project.startDate()).isEqualTo(start);
        assertThat(project.endDate()).isEqualTo(end);
        assertThat(project.leads()).containsExactly(leadId);
    }

    @Test
    void syncFromZep_updatesMutableFields() {
        Project project = Project.create(ProjectId.generate(), profile(10, "Old Name"));
        ZepProjectProfile updated = new ZepProjectProfile(10, "New Name", LocalDate.of(2025, 1, 1), null);

        project.syncFromZep(updated);

        assertThat(project.name()).isEqualTo("New Name");
        assertThat(project.startDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(project.endDate()).isNull();
    }

    @Test
    void syncFromZep_doesNotChangeId() {
        ProjectId id = ProjectId.generate();
        Project project = Project.create(id, profile(10, "X"));

        project.syncFromZep(new ZepProjectProfile(10, "Y", LocalDate.now(), null));

        assertThat(project.id()).isEqualTo(id);
        assertThat(project.zepId()).isEqualTo(10);
    }

    @Test
    void setLeads_replacesLeadsSet() {
        Project project = Project.create(ProjectId.generate(), profile(5, "Gamma"));
        UUID leadA = UUID.randomUUID();
        UUID leadB = UUID.randomUUID();

        project.setLeads(Set.of(leadA, leadB));

        assertThat(project.leads()).containsExactlyInAnyOrder(leadA, leadB);
    }

    @Test
    void setLeads_replacesExistingLeads() {
        UUID oldLead = UUID.randomUUID();
        Project project = Project.reconstitute(ProjectId.generate(), 5, "Delta",
                LocalDate.now(), null, Set.of(oldLead));
        UUID newLead = UUID.randomUUID();

        project.setLeads(Set.of(newLead));

        assertThat(project.leads()).containsExactly(newLead);
        assertThat(project.leads()).doesNotContain(oldLead);
    }

    @Test
    void leads_returnsDefensiveCopy() {
        Project project = Project.create(ProjectId.generate(), profile(1, "X"));
        project.setLeads(Set.of(UUID.randomUUID()));

        Set<UUID> leads = project.leads();
        assertThat(leads).isUnmodifiable();
    }
}
