package com.gepardec.mega.zep.rest.entity;

public class ZepEmploymentBuilder {
    private int id;
    private String name;

    public ZepEmploymentBuilder() {
    }

    public ZepEmploymentBuilder id(int id) {
        this.id = id;
        return this;
    }

    public ZepEmploymentBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ZepEmployment build() {
        return new ZepEmployment(id, name);
    }
}
