package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record ZepDynamicAttribute (String name, String value, Map<String, String> description) {


    @JsonCreator
    public ZepDynamicAttribute(Builder builder) {
        this(builder.name, builder.value, builder.description);
    }

    public static Builder builder() {
        return Builder.aZepDynamicAttribute();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        @JsonProperty
        private String name;
        @JsonProperty
        private String value;
        @JsonProperty
        private Map<String, String> description;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder description(Map<String, String> description) {
            this.description = description;
            return this;
        }

        public ZepDynamicAttribute build() {
            return new ZepDynamicAttribute(this);
        }

        public static Builder aZepDynamicAttribute() {
            return new Builder();
        }
    }
}
