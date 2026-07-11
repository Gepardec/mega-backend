package com.gepardec.mega.hexagon.recognition.application;

import com.gepardec.mega.hexagon.recognition.application.port.inbound.SubmitRecognitionEntryCommand;
import com.gepardec.mega.hexagon.recognition.domain.error.RecognitionValidationException;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionCategory;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntry;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntryStatus;
import com.gepardec.mega.hexagon.recognition.domain.port.outbound.RecognitionEntryRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.instancio.Instancio;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class SubmitRecognitionEntryServiceTest {

    private static final Instant SUBMISSION_TIME = Instant.parse("2026-07-06T15:30:00Z");
    private static final UserId SUBMITTER_ID = UserId.of(Instancio.create(UUID.class));

    private RecognitionEntryRepository recognitionEntryRepository;
    private SubmitRecognitionEntryService service;

    @BeforeEach
    void setUp() {
        recognitionEntryRepository = mock(RecognitionEntryRepository.class);
        service = new SubmitRecognitionEntryService(
                recognitionEntryRepository,
                Clock.fixed(SUBMISSION_TIME, ZoneOffset.UTC)
        );
    }

    @Test
    void submit_shouldPersistSubmitterForNonAnonymousEntry() {
        service.submit(
                new SubmitRecognitionEntryCommand("Danke für den Mut im Kundentermin.", RecognitionCategory.COURAGE, false),
                SUBMITTER_ID
        );

        ArgumentCaptor<RecognitionEntry> entryCaptor = ArgumentCaptor.forClass(RecognitionEntry.class);
        verify(recognitionEntryRepository).save(entryCaptor.capture());
        RecognitionEntry entry = entryCaptor.getValue();
        assertThat(entry.message()).isEqualTo("Danke für den Mut im Kundentermin.");
        assertThat(entry.category()).isEqualTo(RecognitionCategory.COURAGE);
        assertThat(entry.submittedAt()).isEqualTo(SUBMISSION_TIME);
        assertThat(entry.status()).isEqualTo(RecognitionEntryStatus.NEW);
        assertThat(entry.submittedBy()).isEqualTo(SUBMITTER_ID);
    }

    @Test
    void submit_shouldNotPersistSubmitterForAnonymousEntry() {
        service.submit(
                new SubmitRecognitionEntryCommand("Danke für den Mut im Kundentermin.", RecognitionCategory.COURAGE, true),
                SUBMITTER_ID
        );

        ArgumentCaptor<RecognitionEntry> entryCaptor = ArgumentCaptor.forClass(RecognitionEntry.class);
        verify(recognitionEntryRepository).save(entryCaptor.capture());

        assertThat(entryCaptor.getValue().submittedBy()).isNull();
    }

    @Test
    void submit_shouldRejectBlankMessageWithoutPersistingEntry() {
        ThrowableAssert.ThrowingCallable throwingCallable = () -> service.submit(
                new SubmitRecognitionEntryCommand("  ", RecognitionCategory.APPRECIATION, false),
                SUBMITTER_ID
        );

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(RecognitionValidationException.class)
                .hasMessage("message must not be blank");

        verifyNoInteractions(recognitionEntryRepository);
    }
}
