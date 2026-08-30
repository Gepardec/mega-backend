package com.gepardec.mega.hexagon.recognition.application.port.outbound;

import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionMailRecipient;

import java.time.LocalDate;
import java.util.List;

public interface ProjectLeadDirectoryPort {

    List<RecognitionMailRecipient> findActiveInternalProjectLeads(LocalDate referenceDate);
}
