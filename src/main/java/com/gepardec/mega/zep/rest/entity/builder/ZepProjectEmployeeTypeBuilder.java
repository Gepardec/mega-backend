package com.gepardec.mega.zep.rest.entity.builder;

import com.gepardec.mega.zep.rest.entity.ZepProjectEmployeeType;

public class ZepProjectEmployeeTypeBuilder {
    private int id;
    private String name;

    public ZepProjectEmployeeTypeBuilder id(int id) {
        this.id = id;
        return this;
    }

    public ZepProjectEmployeeTypeBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ZepProjectEmployeeType build() {
        return new ZepProjectEmployeeType(id, name);
    }
}
