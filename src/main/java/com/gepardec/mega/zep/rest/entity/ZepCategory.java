package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record ZepCategory (String name, Map<String, String> description) {


    @JsonCreator
    public ZepCategory(Builder builder) {
        this(builder.name, builder.description);
    }


    private static Builder builder() {
        return Builder.aZepCategory();

    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        @JsonProperty
        private String name;
        @JsonProperty
        private Map<String, String> description;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(Map<String, String> description) {
            this.description = description;
            return this;
        }

        public ZepCategory build() {
            return new ZepCategory(this);
        }

        private static Builder aZepCategory() {
            return new Builder();
        }
    }
}
