package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepLanguage {
    private final String id;
    private final String name;

    @JsonCreator
    public ZepLanguage(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
    }

    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public static Builder builder() {
        return Builder.aZepLanguage();
    }

    public static class Builder {
        private String id;
        private String name;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public static Builder aZepLanguage() {
            return new Builder();
        }

        public ZepLanguage build() {
            return new ZepLanguage(this);
        }
    }
}
