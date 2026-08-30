package com.gepardec.mega.hexagon.recognition.domain.port.outbound;

import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntry;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntryStatus;

import java.util.List;

public interface RecognitionEntryRepository {

    void save(RecognitionEntry entry);

    List<RecognitionEntry> findByStatus(RecognitionEntryStatus status);
}
