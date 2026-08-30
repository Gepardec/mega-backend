package com.gepardec.mega.hexagon.recognition.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.Email;

import java.util.Objects;

public record RecognitionMailRecipient(Email email, String firstName) {

    public RecognitionMailRecipient {
        Objects.requireNonNull(email, "email must not be null");
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName must not be blank");
        }
    }
}
