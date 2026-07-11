package com.gepardec.mega.hexagon.recognition.domain.model;

import com.gepardec.mega.hexagon.recognition.domain.error.RecognitionValidationException;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.Instant;
import java.util.Objects;

public record RecognitionEntry(
        RecognitionEntryId id,
        String message,
        RecognitionCategory category,
        Instant submittedAt,
        RecognitionEntryStatus status,
        UserId submittedBy
) {

    public RecognitionEntry {
        Objects.requireNonNull(id, "id must not be null");
        if (message == null || message.isBlank()) {
            throw new RecognitionValidationException("message must not be blank");
        }
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(submittedAt, "submittedAt must not be null");
        Objects.requireNonNull(status, "status must not be null");

        if (message.length() > 500) {
            throw new RecognitionValidationException("message must not exceed 500 characters");
        }
    }

    public static RecognitionEntry create(
            RecognitionEntryId id,
            String message,
            RecognitionCategory category,
            Instant submittedAt
    ) {
        return create(id, message, category, submittedAt, null);
    }

    public static RecognitionEntry create(
            RecognitionEntryId id,
            String message,
            RecognitionCategory category,
            Instant submittedAt,
            UserId submittedBy
    ) {
        return new RecognitionEntry(id, message, category, submittedAt, RecognitionEntryStatus.NEW, submittedBy);
    }

    public RecognitionEntry includeInDigest() {
        if (status == RecognitionEntryStatus.INCLUDED_IN_DIGEST) {
            return this;
        }

        return new RecognitionEntry(id, message, category, submittedAt, RecognitionEntryStatus.INCLUDED_IN_DIGEST, submittedBy);
    }
}
