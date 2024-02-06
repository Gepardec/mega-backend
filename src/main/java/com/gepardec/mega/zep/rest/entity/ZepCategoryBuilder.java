package com.gepardec.mega.zep.rest.entity;

import java.util.Map;

public class ZepCategoryBuilder {
    private String name;
    private Map<String, String> description;

    public ZepCategoryBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ZepCategoryBuilder description(Map<String, String> description) {
        this.description = description;
        return this;
    }

    public ZepCategory build() {
        return new ZepCategory(name, description);
    }
}
