package com.gepardec.mega.hexagon.recognition.application.port.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.util.Map;
import java.util.Set;

public interface RecognitionSubmitterDirectoryPort {

    Map<UserId, String> findDisplayNamesByIds(Set<UserId> userIds);
}
