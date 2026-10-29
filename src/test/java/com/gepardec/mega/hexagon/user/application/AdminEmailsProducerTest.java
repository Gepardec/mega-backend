package com.gepardec.mega.hexagon.user.application;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AdminEmailsProducerTest {

    @Test
    void adminEmails_shouldContainConfiguredEmails() {
        var producer = new AdminEmailsProducer(Optional.of(List.of("admin@gepardec.com")));

        assertThat(producer.adminEmails().contains("ADMIN@gepardec.com")).isTrue();
    }

    @Test
    void adminEmails_shouldBeEmptyWhenListIsAbsent() {
        var producer = new AdminEmailsProducer(Optional.empty());

        assertThat(producer.adminEmails().emails()).isEmpty();
    }
}
