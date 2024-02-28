package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ZepLanguage (String id, String name) {

    @JsonCreator
    public ZepLanguage(Builder builder) {
        this(builder.id, builder.name);
    }

    public static Builder builder() {
        return Builder.aZepLanguage();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        @JsonProperty
        private String id;
        @JsonProperty
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
