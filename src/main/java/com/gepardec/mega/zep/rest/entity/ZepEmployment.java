package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepEmployment {
    private final int id;
    private final String name;

    @JsonCreator
    public ZepEmployment(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
    }

    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public static Builder builder() {
        return Builder.aZepEmployment();
    }

    public static class Builder {
        private int id;
        private String name;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public ZepEmployment build() {
            return new ZepEmployment(this);
        }

        public static Builder aZepEmployment() {
            return new Builder();
        }
    }
}
