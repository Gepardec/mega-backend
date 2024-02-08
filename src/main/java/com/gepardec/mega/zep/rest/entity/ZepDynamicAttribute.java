package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepDynamicAttribute {
    private final String name;
    private final String value;
    private final Map<String, String> description;


    @JsonCreator
    public ZepDynamicAttribute(Builder builder) {
        this.name = builder.name;
        this.value = builder.value;
        this.description = builder.description;
    }

    public String getName() {
        return name;
    }


    public String getValue() {
        return value;
    }


    public Map<String, String> getDescription() {
        return description;
    }


    public static Builder builder() {
        return Builder.aZepDynamicAttribute();
    }
    
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
