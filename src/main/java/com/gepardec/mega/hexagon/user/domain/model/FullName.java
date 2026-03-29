package com.gepardec.mega.hexagon.user.domain.model;

public record FullName(String firstname, String lastname) {

    public static FullName of(String firstname, String lastname) {
        return new FullName(firstname, lastname);
    }
}
