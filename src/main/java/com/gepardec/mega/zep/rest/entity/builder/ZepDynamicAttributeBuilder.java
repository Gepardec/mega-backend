package com.gepardec.mega.zep.rest.entity.builder;

import com.gepardec.mega.zep.rest.entity.ZepDynamicAttribute;

import java.util.Map;

public class ZepDynamicAttributeBuilder {
    private String name;
    private String value;
    private Map<String, String> description;

    public ZepDynamicAttributeBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ZepDynamicAttributeBuilder value(String value) {
        this.value = value;
        return this;
    }

    public ZepDynamicAttributeBuilder description(Map<String, String> description) {
        this.description = description;
        return this;
    }

    public ZepDynamicAttribute build() {
        return new ZepDynamicAttribute(name, value, description);
    }
}
