package com.gepardec.mega.hexagon.user.domain.model;

public record HourlyRate(double value) {

    public HourlyRate {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException("hourlyRate must be greater than zero");
        }
    }

    public static HourlyRate of(double value) {
        return new HourlyRate(value);
    }
}
