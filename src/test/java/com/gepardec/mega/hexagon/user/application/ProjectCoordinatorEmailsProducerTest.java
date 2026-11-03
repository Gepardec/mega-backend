package com.gepardec.mega.hexagon.user.application;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectCoordinatorEmailsProducerTest {

    @Test
    void projectCoordinatorEmails_shouldContainConfiguredEmails() {
        var producer = new ProjectCoordinatorEmailsProducer(Optional.of(List.of("coordinator@gepardec.com")));

        assertThat(producer.projectCoordinatorEmails().contains("COORDINATOR@gepardec.com")).isTrue();
    }

    @Test
    void projectCoordinatorEmails_shouldBeEmptyWhenListIsAbsent() {
        var producer = new ProjectCoordinatorEmailsProducer(Optional.empty());

        assertThat(producer.projectCoordinatorEmails().emails()).isEmpty();
    }
}
