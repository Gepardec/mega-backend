package com.gepardec.mega.hexagon.recognition.domain.model;

import java.util.UUID;

public record RecognitionEntryId(UUID value) {

    public static RecognitionEntryId generate() {
        return new RecognitionEntryId(UUID.randomUUID());
    }

    public static RecognitionEntryId of(UUID value) {
        return new RecognitionEntryId(value);
    }
}
