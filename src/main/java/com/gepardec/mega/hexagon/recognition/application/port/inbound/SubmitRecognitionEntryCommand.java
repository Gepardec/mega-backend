package com.gepardec.mega.hexagon.recognition.application.port.inbound;

import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionCategory;

public record SubmitRecognitionEntryCommand(
        String message,
        RecognitionCategory category,
        boolean anonymous
) {
}
