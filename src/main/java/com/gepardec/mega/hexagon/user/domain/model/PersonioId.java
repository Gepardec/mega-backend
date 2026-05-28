package com.gepardec.mega.hexagon.user.domain.model;

public record PersonioId(int value) {

    public static PersonioId of(int value) {
        return new PersonioId(value);
    }
}
