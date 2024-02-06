package com.gepardec.mega.zep.rest.entity;

import java.util.Map;

public class ZepDynamicAttribute {
    private String name;
    private String value;
    private Map<String, String> description;

    public ZepDynamicAttribute() {}

    public ZepDynamicAttribute(String name, String value, Map<String, String> description) {
        this.name = name;
        this.value = value;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public static ZepDynamicAttributeBuilder builder() {
        return new ZepDynamicAttributeBuilder();
    }
}
