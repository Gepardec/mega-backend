package com.gepardec.mega.hexagon.worktime.domain.model;

import java.util.Arrays;
import java.util.Optional;

public enum Vehicle {
    CAR_ACTIVE("Auto", true),
    CAR_INACTIVE("Auto (PKW passiv)", false),
    OTHER_INACTIVE("", false);

    private final String id;
    private final boolean activeTraveler;

    Vehicle(String id, boolean activeTraveler) {
        this.id = id;
        this.activeTraveler = activeTraveler;
    }

    public static Optional<Vehicle> forId(String id) {
        String normalized = id == null ? "" : id;
        return Arrays.stream(values()).filter(vehicle -> vehicle.id.equals(normalized)).findFirst();
    }

    public String getId() {
        return id;
    }

    public boolean isActiveTraveler() {
        return activeTraveler;
    }
}
