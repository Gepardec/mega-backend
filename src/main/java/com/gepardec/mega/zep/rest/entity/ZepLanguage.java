package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gepardec.mega.zep.rest.entity.builder.ZepLanguageBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepLanguage {
    private String id;
    private String name;

    public ZepLanguage() {
    }

    public ZepLanguage(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static ZepLanguageBuilder builder() {
        return new ZepLanguageBuilder();
    }
}
