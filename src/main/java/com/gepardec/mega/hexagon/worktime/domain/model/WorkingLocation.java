package com.gepardec.mega.hexagon.worktime.domain.model;

import java.util.Arrays;

public enum WorkingLocation {
    MAIN("- erste Tätigkeitsstätte -"),
    A("A"),
    OTHER("OTHER");

    private final String zepLocation;

    WorkingLocation(String zepLocation) {
        this.zepLocation = zepLocation;
    }

    public static WorkingLocation fromZepOrt(String zepLocation) {
        return Arrays.stream(values())
                .filter(location -> location.zepLocation.equals(zepLocation))
                .findFirst()
                .orElse(OTHER);
    }

    public String getZepOrt() {
        return zepLocation;
    }
}
