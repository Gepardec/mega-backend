package com.gepardec.mega.zep.rest.entity;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ZepAbsenceReasonType (Integer id, String name) {

    @JsonCreator
    public ZepAbsenceReasonType(Builder builder) {
        this(builder.id, builder.name);
    }

    public static Builder builder() {
        return Builder.aZepAbsenceReasonType();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        @JsonProperty
        private Integer id;
        @JsonProperty
        private String name;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public ZepAbsenceReasonType build() {
            return new ZepAbsenceReasonType(this);
        }

        public static Builder aZepAbsenceReasonType() {
            return new Builder();
        }
    }
}
