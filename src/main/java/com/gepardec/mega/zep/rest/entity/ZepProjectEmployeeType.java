package com.gepardec.mega.zep.rest.entity;

import lombok.*;


public class ZepProjectEmployeeType {
    private int id;

    private String name;

    public ZepProjectEmployeeType() {
    }

    public ZepProjectEmployeeType(int id, String name) {
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

    public static ZepProjectEmployeeTypeBuilder builder() {
        return new ZepProjectEmployeeTypeBuilder();
    }
}
