package com.gepardec.mega.zep.rest.entity;

public class ZepSalutation {
    public String id;
    public String name;

    public ZepSalutation() {}

    public ZepSalutation(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static ZepSalutationBuilder builder() {
        return new ZepSalutationBuilder();
    }
}
