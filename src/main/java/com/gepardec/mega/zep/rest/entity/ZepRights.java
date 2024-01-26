package com.gepardec.mega.zep.rest.entity;

public class ZepRights {
    private int id;

    private String name;

    public ZepRights() {
    }

    public ZepRights(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static ZepRightsBuilder builder() {
        return new ZepRightsBuilder();
    }
}
