package com.gepardec.mega.zep.rest.entity;

public class ZepRightsBuilder {
    private int id;
    private String name;

    public ZepRightsBuilder id(int id) {
        this.id = id;
        return this;
    }

    public ZepRightsBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ZepRights build() {
        return new ZepRights(id, name);
    }
}
