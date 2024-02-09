package com.gepardec.mega.zep.rest.entity;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ZepAbsenceReasonType {
    private final Integer id;
    private final String name;


    @JsonCreator
    public ZepAbsenceReasonType(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
    }

    public Integer getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public static Builder builder() {
        return Builder.aZepAbsenceReasonType();
    }

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
