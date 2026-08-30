package com.gepardec.mega.hexagon.recognition.application.model;

import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionCategory;

import java.util.Objects;

public record RecognitionDigestEntry(
        String message,
        RecognitionCategory category,
        String submitterName
) {

    public RecognitionDigestEntry {
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(submitterName, "submitterName must not be null");
    }
}
