package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ZepProjectEmployeeType (int id) {

    @JsonCreator
    public ZepProjectEmployeeType(Builder builder) {
        this(builder.id);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)

    public static final class Builder {
        @JsonProperty
        private int id;

        public static Builder aZepProjectEmployeeType() {
            return new Builder();
        }

        public Builder id(int id) {
            this.id = id;
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
