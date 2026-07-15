package com.gepardec.mega.hexagon.recognition.application.port.inbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;

public interface SubmitRecognitionEntryUseCase {

    void submit(SubmitRecognitionEntryCommand command, UserId submitterId);
}
