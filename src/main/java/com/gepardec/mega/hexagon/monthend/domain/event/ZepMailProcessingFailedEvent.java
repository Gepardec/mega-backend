package com.gepardec.mega.hexagon.monthend.domain.event;

import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

public record ZepMailProcessingFailedEvent(
        UserId creatorUserId,
        Email creatorEmail,
        String originalRecipient,
        String errorMessage,
        String rawMailContent
) {
}
