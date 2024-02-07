package com.gepardec.mega.zep.rest.entity.builder;

import com.gepardec.mega.zep.rest.entity.ZepSalutation;

public class ZepSalutationBuilder {
    private String id;
    private String name;

    public ZepSalutationBuilder id(String id) {
        this.id = id;
        return this;
    }

    public ZepSalutationBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ZepSalutation build() {
        return new ZepSalutation(id, name);
    }

}