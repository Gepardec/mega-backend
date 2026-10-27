package com.gepardec.mega.hexagon.user.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AdminEmailsTest {

    @Test
    void contains_shouldIgnoreCaseAndWhitespace() {
        AdminEmails emails = new AdminEmails(Set.of(" Admin@Gepardec.com "));

        assertThat(emails.contains("admin@gepardec.com")).isTrue();
        assertThat(emails.contains("  ADMIN@GEPARDEC.COM ")).isTrue();
    }

    @Test
    void contains_shouldReturnFalseWhenEmailIsNotConfigured() {
        AdminEmails emails = new AdminEmails(Set.of("admin@gepardec.com"));

        assertThat(emails.contains("employee@gepardec.com")).isFalse();
    }

    @Test
    void contains_shouldReturnFalseForEmptyList() {
        assertThat(new AdminEmails(Set.of()).contains("admin@gepardec.com")).isFalse();
    }
}
