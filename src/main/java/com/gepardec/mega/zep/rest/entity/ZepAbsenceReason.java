package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public record ZepAbsenceReason (
    String name
) {
    @JsonCreator
    public ZepAbsenceReason(Builder builder) {
        this(builder.name);
    }

    public static Builder builder() {
        return Builder.aZepAbsenceReason();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        @JsonProperty
        public String name;


        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public ZepAbsenceReason build() {
            return new ZepAbsenceReason(this);
        }

        public static Builder aZepAbsenceReason() {
            return new Builder();
        }
    }

}
