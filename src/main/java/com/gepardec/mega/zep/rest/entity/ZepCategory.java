package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gepardec.mega.zep.rest.entity.builder.ZepCategoryBuilder;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
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
