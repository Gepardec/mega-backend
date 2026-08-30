package com.gepardec.mega.hexagon.recognition.application;

import com.gepardec.mega.hexagon.recognition.application.port.outbound.ProjectLeadDirectoryPort;
import com.gepardec.mega.hexagon.recognition.application.port.outbound.RecognitionMailPort;
import com.gepardec.mega.hexagon.recognition.application.port.outbound.RecognitionSubmitterDirectoryPort;
import com.gepardec.mega.hexagon.recognition.application.model.RecognitionDigestEntry;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionCategory;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntry;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntryId;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntryStatus;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionMailRecipient;
import com.gepardec.mega.hexagon.recognition.domain.port.outbound.RecognitionEntryRepository;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RecognitionDigestServiceTest {

    private static final Instant NOW = Instant.parse("2026-07-06T15:30:00Z");
    private static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 7, 6);

    private RecognitionEntryRepository recognitionEntryRepository;
    private ProjectLeadDirectoryPort projectLeadDirectoryPort;
    private RecognitionSubmitterDirectoryPort recognitionSubmitterDirectoryPort;
    private RecognitionMailPort recognitionMailPort;
    private RecognitionDigestService service;

    @BeforeEach
    void setUp() {
        recognitionEntryRepository = mock(RecognitionEntryRepository.class);
        projectLeadDirectoryPort = mock(ProjectLeadDirectoryPort.class);
        recognitionSubmitterDirectoryPort = mock(RecognitionSubmitterDirectoryPort.class);
        recognitionMailPort = mock(RecognitionMailPort.class);
        service = new RecognitionDigestService(
                recognitionEntryRepository,
                projectLeadDirectoryPort,
                recognitionSubmitterDirectoryPort,
                recognitionMailPort,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void sendDigest_shouldSendNewEntriesToEveryRecipientAndMarkThemIncluded() {
        RecognitionEntry entry = entry("Großartige Unterstützung im Projekt.", RecognitionCategory.APPRECIATION);
        RecognitionMailRecipient firstRecipient = recipient("lead-one@example.com", "Ada");
        RecognitionMailRecipient secondRecipient = recipient("lead-two@example.com", "Grace");
        when(projectLeadDirectoryPort.findActiveInternalProjectLeads(REFERENCE_DATE))
                .thenReturn(List.of(firstRecipient, secondRecipient));
        when(recognitionEntryRepository.findByStatus(RecognitionEntryStatus.NEW)).thenReturn(List.of(entry));
        when(recognitionSubmitterDirectoryPort.findDisplayNamesByIds(Set.of(entry.submittedBy())))
                .thenReturn(Map.of(entry.submittedBy(), "Grace Hopper"));

        service.sendDigest();

        verify(projectLeadDirectoryPort).findActiveInternalProjectLeads(REFERENCE_DATE);
        List<RecognitionDigestEntry> digestEntries = List.of(new RecognitionDigestEntry(
                entry.message(), entry.category(), "Grace Hopper"
        ));
        verify(recognitionMailPort).sendDigest(firstRecipient, digestEntries);
        verify(recognitionMailPort).sendDigest(secondRecipient, digestEntries);
        verify(recognitionEntryRepository).save(entry.includeInDigest());
    }

    @Test
    void sendDigest_shouldSendEmptyStateWithoutChangingEntryStatusWhenNoNewEntriesExist() {
        RecognitionMailRecipient recipient = recipient("lead@example.com", "Ada");
        when(projectLeadDirectoryPort.findActiveInternalProjectLeads(REFERENCE_DATE)).thenReturn(List.of(recipient));
        when(recognitionEntryRepository.findByStatus(RecognitionEntryStatus.NEW)).thenReturn(List.of());

        service.sendDigest();

        verify(recognitionMailPort).sendDigest(recipient, List.of());
        verify(recognitionEntryRepository, never()).save(any());
        verifyNoInteractions(recognitionSubmitterDirectoryPort);
    }

    @Test
    void sendDigest_shouldDoNothingWhenNoRecipientsExist() {
        when(projectLeadDirectoryPort.findActiveInternalProjectLeads(REFERENCE_DATE)).thenReturn(List.of());

        service.sendDigest();

        verify(projectLeadDirectoryPort).findActiveInternalProjectLeads(REFERENCE_DATE);
        verifyNoInteractions(recognitionEntryRepository, recognitionSubmitterDirectoryPort, recognitionMailPort);
    }

    @Test
    void sendDigest_shouldLeaveEntriesNewWhenMailDispatchFails() {
        RecognitionEntry entry = entry("Mutiger Einsatz", RecognitionCategory.COURAGE);
        RecognitionMailRecipient recipient = recipient("lead@example.com", "Ada");
        when(projectLeadDirectoryPort.findActiveInternalProjectLeads(REFERENCE_DATE)).thenReturn(List.of(recipient));
        when(recognitionEntryRepository.findByStatus(RecognitionEntryStatus.NEW)).thenReturn(List.of(entry));
        when(recognitionSubmitterDirectoryPort.findDisplayNamesByIds(Set.of(entry.submittedBy())))
                .thenReturn(Map.of(entry.submittedBy(), "Grace Hopper"));
        doThrow(new IllegalStateException("mail dispatch failed"))
                .when(recognitionMailPort).sendDigest(recipient, List.of(new RecognitionDigestEntry(
                        entry.message(), entry.category(), "Grace Hopper"
                )));

        assertThatThrownBy(service::sendDigest)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("mail dispatch failed");

        verify(recognitionEntryRepository, never()).save(any());
    }

    @Test
    void sendDigest_shouldUseAnonymWhenEntryHasNoSubmitter() {
        RecognitionEntry entry = RecognitionEntry.create(
                RecognitionEntryId.of(Instancio.create(UUID.class)),
                "Mutiger Einsatz",
                RecognitionCategory.COURAGE,
                NOW,
                null
        );
        RecognitionMailRecipient recipient = recipient("lead@example.com", "Ada");
        when(projectLeadDirectoryPort.findActiveInternalProjectLeads(REFERENCE_DATE)).thenReturn(List.of(recipient));
        when(recognitionEntryRepository.findByStatus(RecognitionEntryStatus.NEW)).thenReturn(List.of(entry));

        service.sendDigest();

        verify(recognitionMailPort).sendDigest(recipient, List.of(new RecognitionDigestEntry(
                entry.message(), entry.category(), "Anonym"
        )));
        verifyNoInteractions(recognitionSubmitterDirectoryPort);
    }

    private RecognitionEntry entry(String message, RecognitionCategory category) {
        return RecognitionEntry.create(
                RecognitionEntryId.of(Instancio.create(UUID.class)),
                message,
                category,
                NOW,
                UserId.of(Instancio.create(UUID.class))
        );
    }

    private RecognitionMailRecipient recipient(String email, String firstName) {
        return new RecognitionMailRecipient(Email.of(email), firstName);
    }
}
