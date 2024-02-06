package com.gepardec.mega.zep.rest.entity;

import java.util.Map;

public class ZepCategory {
    private String name;

    private Map<String, String> description;

    public ZepCategory() {
    }

    public ZepCategory(String name, Map<String, String> description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    private static ZepCategoryBuilder builder() {
        return new ZepCategoryBuilder();
    }
}
