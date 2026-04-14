package com.gepardec.mega.hexagon.shared.domain.model;

public record Email(String value) {

    public static Email of(String value) {
        return new Email(value);
    }
}
