package com.gepardec.mega.zep.rest.entity.builder;

import com.gepardec.mega.zep.rest.entity.ZepAbsenceReasonType;

public class ZepAbsenceReasonTypeBuilder {
    private Integer id;
    private String name;

    public ZepAbsenceReasonTypeBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public ZepAbsenceReasonTypeBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ZepAbsenceReasonType build() {
        return new ZepAbsenceReasonType(id, name);
    }
}
