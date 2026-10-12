package com.gepardec.mega.hexagon.user.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OfficeManagementEmailsTest {

    @Test
    void contains_shouldMatchExactEmail() {
        OfficeManagementEmails emails = new OfficeManagementEmails(Set.of("om@example.com"));

        assertThat(emails.contains("om@example.com")).isTrue();
    }

    @Test
    void contains_shouldIgnoreCase() {
        OfficeManagementEmails emails = new OfficeManagementEmails(Set.of("om@example.com"));

        assertThat(emails.contains("OM@EXAMPLE.COM")).isTrue();
    }

    @Test
    void contains_shouldTrimWhitespace() {
        OfficeManagementEmails emails = new OfficeManagementEmails(Set.of("om@example.com"));

        assertThat(emails.contains("  om@example.com  ")).isTrue();
    }

    @Test
    void contains_shouldReturnFalseWhenEmailIsNotConfigured() {
        OfficeManagementEmails emails = new OfficeManagementEmails(Set.of("om@example.com"));

        assertThat(emails.contains("employee@example.com")).isFalse();
    }
}
