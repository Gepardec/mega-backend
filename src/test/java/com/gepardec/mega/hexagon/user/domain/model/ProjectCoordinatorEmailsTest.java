package com.gepardec.mega.hexagon.user.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectCoordinatorEmailsTest {

    @Test
    void contains_shouldIgnoreCaseAndWhitespace() {
        ProjectCoordinatorEmails emails = new ProjectCoordinatorEmails(Set.of(" Coordinator@Gepardec.com "));

        assertThat(emails.contains("coordinator@gepardec.com")).isTrue();
        assertThat(emails.contains("  COORDINATOR@GEPARDEC.COM ")).isTrue();
    }

    @Test
    void contains_shouldReturnFalseWhenEmailIsNotConfigured() {
        ProjectCoordinatorEmails emails = new ProjectCoordinatorEmails(Set.of("coordinator@gepardec.com"));

        assertThat(emails.contains("employee@gepardec.com")).isFalse();
    }

    @Test
    void contains_shouldReturnFalseForEmptyList() {
        assertThat(new ProjectCoordinatorEmails(Set.of()).contains("coordinator@gepardec.com")).isFalse();
    }
}
