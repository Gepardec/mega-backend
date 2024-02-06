package com.gepardec.mega.zep.rest.entity;

import com.gepardec.mega.zep.rest.entity.builder.ZepAbsenceReasonTypeBuilder;

public class ZepAbsenceReasonType {
    private Integer id;
    private String name;

    public ZepAbsenceReasonType() {
    }

    public ZepAbsenceReasonType(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static ZepAbsenceReasonTypeBuilder builder() {
        return new ZepAbsenceReasonTypeBuilder();
    }
}
