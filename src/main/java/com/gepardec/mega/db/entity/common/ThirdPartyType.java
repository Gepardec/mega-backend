package com.gepardec.mega.db.entity.common;

public enum ThirdPartyType {
    PERSONIO(0, "Personio"),
    ZEP(1, "Zep");

    final int id;
    final String name;

    ThirdPartyType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
