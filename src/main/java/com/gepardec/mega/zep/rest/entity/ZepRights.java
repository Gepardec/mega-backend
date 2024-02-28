package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ZepRights (int id, String name) {
    @JsonCreator
    public ZepRights(Builder builder) {
        this(builder.id, builder.name);
    }

    public static Builder builder() {
        return Builder.aZepRights();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        @JsonProperty
        private int id;
        @JsonProperty
        private String name;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public ZepRights build() {
            return new ZepRights(this);
        }

        public static Builder aZepRights() {
            return new Builder();
        }
    }

}
