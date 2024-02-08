package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gepardec.mega.zep.rest.entity.builder.ZepRightsBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepRights {
    private final int id;

    private final String name;

    @JsonCreator
    public ZepRights(Builder builder) {
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
        return Builder.aZepRights();
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

        public ZepRights build() {
            return new ZepRights(this);
        }

        public static Builder aZepRights() {
            return new Builder();
        }
    }
}
