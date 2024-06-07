package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ZepBillingType(Integer id) {

    @JsonCreator
    ZepBillingType(Builder builder) {
        this(builder.id);
    }

    public static Builder builder() {
        return Builder.aBillingType();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        @JsonProperty
        private Integer id;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public ZepBillingType build() {
            return new ZepBillingType(this);
        }

        public static Builder aBillingType() {
            return new Builder();
        }
    }
}
