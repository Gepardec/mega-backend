package com.gepardec.mega.hexagon.recognition.application;

import com.gepardec.mega.hexagon.recognition.application.port.inbound.SubmitRecognitionEntryCommand;
import com.gepardec.mega.hexagon.recognition.application.port.inbound.SubmitRecognitionEntryUseCase;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntry;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntryId;
import com.gepardec.mega.hexagon.recognition.domain.port.outbound.RecognitionEntryRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@ApplicationScoped
@Transactional
public class SubmitRecognitionEntryService implements SubmitRecognitionEntryUseCase {

    private final RecognitionEntryRepository recognitionEntryRepository;
    private final Clock clock;

    @Inject
    public SubmitRecognitionEntryService(RecognitionEntryRepository recognitionEntryRepository, Clock clock) {
        this.recognitionEntryRepository = recognitionEntryRepository;
        this.clock = clock;
    }

    @Override
    public void submit(SubmitRecognitionEntryCommand command, UserId submitterId) {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(submitterId, "submitterId must not be null");

        RecognitionEntry entry = RecognitionEntry.create(
                RecognitionEntryId.generate(),
                command.message(),
                command.category(),
                Instant.now(clock),
                command.anonymous() ? null : submitterId
        );
        recognitionEntryRepository.save(entry);
    }
}
