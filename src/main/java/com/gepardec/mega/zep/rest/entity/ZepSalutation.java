package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepSalutation {
    public final String id;
    public final String name;


    @JsonCreator
    public ZepSalutation(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
    }

    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public static Builder builder() {
        return Builder.aZepSalutation();
    }

    public static class Builder {
        private String id;
        private String name;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

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
