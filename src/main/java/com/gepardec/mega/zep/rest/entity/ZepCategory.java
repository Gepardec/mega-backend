package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepCategory {
    private final String name;
    private final Map<String, String> description;


    @JsonCreator
    public ZepCategory(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
    }

    public String getName() {
        return name;
    }


    public Map<String, String> getDescription() {
        return description;
    }


    private static Builder builder() {
        return Builder.aZepCategory();
    }

    public static class Builder {
        private String name;
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
