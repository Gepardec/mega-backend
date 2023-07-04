package com.gepardec.mega.domain.model.monthlyreport;

import java.util.stream.Stream;

public enum WorkingLocation {
    MAIN("- erste Tätigkeitsstätte -"),
    A("A"),
    OTHER("OTHER");

    public final String zepOrt;

    WorkingLocation(String zepOrt) {
        this.zepOrt = zepOrt;
    }

    public static WorkingLocation fromZepOrt(String zepOrt) {
        return Stream.of(WorkingLocation.values())
                .filter(workingLocation -> workingLocation.getZepOrt().equals(zepOrt))
                .findAny()
                .orElse(WorkingLocation.OTHER);
    }

    public String getZepOrt() {
        return zepOrt;
    }
}
