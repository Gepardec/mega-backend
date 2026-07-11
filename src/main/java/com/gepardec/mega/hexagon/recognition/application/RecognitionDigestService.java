package com.gepardec.mega.hexagon.recognition.application;

import com.gepardec.mega.hexagon.recognition.application.port.inbound.SendRecognitionDigestUseCase;
import com.gepardec.mega.hexagon.recognition.application.port.outbound.ProjectLeadDirectoryPort;
import com.gepardec.mega.hexagon.recognition.application.port.outbound.RecognitionMailPort;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntry;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntryStatus;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionMailRecipient;
import com.gepardec.mega.hexagon.recognition.domain.port.outbound.RecognitionEntryRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
@Transactional
public class RecognitionDigestService implements SendRecognitionDigestUseCase {

    private final RecognitionEntryRepository recognitionEntryRepository;
    private final ProjectLeadDirectoryPort projectLeadDirectoryPort;
    private final RecognitionMailPort recognitionMailPort;
    private final Clock clock;

    @Inject
    public RecognitionDigestService(
            RecognitionEntryRepository recognitionEntryRepository,
            ProjectLeadDirectoryPort projectLeadDirectoryPort,
            RecognitionMailPort recognitionMailPort,
            Clock clock
    ) {
        this.recognitionEntryRepository = recognitionEntryRepository;
        this.projectLeadDirectoryPort = projectLeadDirectoryPort;
        this.recognitionMailPort = recognitionMailPort;
        this.clock = clock;
    }

    @Override
    public void sendDigest() {
        LocalDate referenceDate = LocalDate.now(clock);
        List<RecognitionMailRecipient> recipients = projectLeadDirectoryPort.findActiveInternalProjectLeads(referenceDate);
        if (recipients.isEmpty()) {
            Log.infof("Skipping recognition digest on %s because no active internal project leads were found", referenceDate);
            return;
        }

        List<RecognitionEntry> entries = recognitionEntryRepository.findByStatus(RecognitionEntryStatus.NEW);
        for (RecognitionMailRecipient recipient : recipients) {
            recognitionMailPort.sendDigest(recipient, entries);
        }

        for (RecognitionEntry entry : entries) {
            recognitionEntryRepository.save(entry.includeInDigest());
        }

        Log.infof("Sent recognition digest to %d recipient(s) with %d new entry/entries", recipients.size(), entries.size());
    }
}
