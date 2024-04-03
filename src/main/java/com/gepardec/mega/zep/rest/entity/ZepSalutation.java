package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ZepSalutation (String name) {

    @JsonCreator
    public ZepSalutation(Builder builder) {
        this(builder.name);
    }

    public static Builder builder() {
        return Builder.aZepSalutation();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        @JsonProperty
        private String name;


        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public ZepSalutation build() {
            return new ZepSalutation(this);
        }

        public static Builder aZepSalutation() {
            return new Builder();
        }
    }
}
