package com.gepardec.mega.hexagon.shared.domain.model;

public record FullName(String firstname, String lastname) {

    public static FullName of(String firstname, String lastname) {
        return new FullName(firstname, lastname);
    }

    public String displayName() {
        if (firstname == null) {
            return lastname;
        }
        if (lastname == null) {
            return firstname;
        }
        return "%s %s".formatted(firstname, lastname);
    }
}
