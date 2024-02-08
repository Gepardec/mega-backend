package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.zep.rest.entity.builder.ZepProjectEmployeeTypeBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepProjectEmployeeType {
    private final int id;

    private final String name;


    @JsonCreator
    public ZepProjectEmployeeType(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
    }

    public static final class Builder {
        @JsonProperty
        private int id;

        @JsonProperty
        private String name;

        public static Builder aZepProjectEmployeeType() {
            return new Builder();
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public ZepProjectEmployeeType build() {
            return new ZepProjectEmployeeType(this);
        }
    }

    public static Builder builder() {
        return Builder.aZepProjectEmployeeType();
    }
}
