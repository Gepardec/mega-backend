package com.gepardec.mega.hexagon.recognition.application.port.outbound;

import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntry;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionMailRecipient;

import java.util.List;

public interface RecognitionMailPort {

    void sendDigest(RecognitionMailRecipient recipient, List<RecognitionEntry> entries);
}
