package com.gepardec.mega.zep.rest.entity;

import lombok.*;

public class ZepEmployment {
    private int id;
    private String name;

    public ZepEmployment() {
    }

    public ZepEmployment(int id, String name) {
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

    public static ZepEmploymentBuilder builder() {
        return new ZepEmploymentBuilder();
    }
}
