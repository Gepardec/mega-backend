package com.gepardec.mega.hexagon.user.domain.model;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record ProjectCoordinatorEmails(Set<String> emails) {

    public ProjectCoordinatorEmails {
        Objects.requireNonNull(emails, "emails must not be null");
        emails = emails.stream()
                .filter(Objects::nonNull)
                .map(ProjectCoordinatorEmails::normalize)
                .filter(email -> !email.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean contains(String email) {
        return emails.contains(normalize(email));
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
