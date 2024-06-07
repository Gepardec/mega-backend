package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ZepBillable(Integer id) {

    @JsonCreator
    public ZepBillable(Builder builder) {
        this(builder.id);
    }

    public static Builder builder() {
        return Builder.aZepBillable();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        @JsonProperty("id")
        private Integer id;

        public ZepBillable build() {
            return new ZepBillable(this);
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public static Builder aZepBillable() {
            return new Builder();
        }
    }
}
