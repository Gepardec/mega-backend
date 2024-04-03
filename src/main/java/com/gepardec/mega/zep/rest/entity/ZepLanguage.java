package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ZepLanguage (String id) {

    @JsonCreator
    public ZepLanguage(Builder builder) {
        this(builder.id);
    }

    public static Builder builder() {
        return Builder.aZepLanguage();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        @JsonProperty
        private String id;

        public Builder id(String id) {
            this.id = id;
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
