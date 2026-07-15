package com.gepardec.mega.hexagon.recognition.application;

import com.gepardec.mega.hexagon.recognition.application.model.RecognitionDigestEntry;
import com.gepardec.mega.hexagon.recognition.application.port.inbound.SendRecognitionDigestUseCase;
import com.gepardec.mega.hexagon.recognition.application.port.outbound.ProjectLeadDirectoryPort;
import com.gepardec.mega.hexagon.recognition.application.port.outbound.RecognitionMailPort;
import com.gepardec.mega.hexagon.recognition.application.port.outbound.RecognitionSubmitterDirectoryPort;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntry;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntryStatus;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionMailRecipient;
import com.gepardec.mega.hexagon.recognition.domain.port.outbound.RecognitionEntryRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class RecognitionDigestService implements SendRecognitionDigestUseCase {

    private static final String ANONYMOUS_SUBMITTER_NAME = "Anonym";

    private final RecognitionEntryRepository recognitionEntryRepository;
    private final ProjectLeadDirectoryPort projectLeadDirectoryPort;
    private final RecognitionSubmitterDirectoryPort recognitionSubmitterDirectoryPort;
    private final RecognitionMailPort recognitionMailPort;
    private final Clock clock;

    @Inject
    public RecognitionDigestService(
            RecognitionEntryRepository recognitionEntryRepository,
            ProjectLeadDirectoryPort projectLeadDirectoryPort,
            RecognitionSubmitterDirectoryPort recognitionSubmitterDirectoryPort,
            RecognitionMailPort recognitionMailPort,
            Clock clock
    ) {
        this.recognitionEntryRepository = recognitionEntryRepository;
        this.projectLeadDirectoryPort = projectLeadDirectoryPort;
        this.recognitionSubmitterDirectoryPort = recognitionSubmitterDirectoryPort;
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
        List<RecognitionDigestEntry> digestEntries = toDigestEntries(entries);
        for (RecognitionMailRecipient recipient : recipients) {
            recognitionMailPort.sendDigest(recipient, digestEntries);
        }

        for (RecognitionEntry entry : entries) {
            recognitionEntryRepository.save(entry.includeInDigest());
        }

        Log.infof("Sent recognition digest to %d recipient(s) with %d new entry/entries", recipients.size(), entries.size());
    }

    private List<RecognitionDigestEntry> toDigestEntries(List<RecognitionEntry> entries) {
        Set<UserId> submitterIds = entries.stream()
                .map(RecognitionEntry::submittedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UserId, String> submitterNames = submitterIds.isEmpty()
                ? Map.of()
                : recognitionSubmitterDirectoryPort.findDisplayNamesByIds(submitterIds);

        return entries.stream()
                .map(entry -> new RecognitionDigestEntry(
                        entry.message(),
                        entry.category(),
                        entry.submittedBy() == null
                                ? ANONYMOUS_SUBMITTER_NAME
                                : requireSubmitterName(entry, submitterNames)
                ))
                .toList();
    }

    private String requireSubmitterName(RecognitionEntry entry, Map<UserId, String> submitterNames) {
        String submitterName = submitterNames.get(entry.submittedBy());
        if (submitterName == null) {
            throw new IllegalStateException("Could not resolve recognition submitter '%s'".formatted(entry.submittedBy().value()));
        }
        return submitterName;
    }
}
