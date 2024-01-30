package com.gepardec.mega.zep.rest.entity;

public class ZepLanguageBuilder {
    private String id;
    private String name;

    public ZepLanguageBuilder id(String id) {
        this.id = id;
        return this;
    }

    public ZepLanguageBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ZepLanguage build() {
        return new ZepLanguage(id, name);
    }
}
