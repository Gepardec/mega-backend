package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)

public record ZepProjectEmployee (String username, boolean lead, ZepProjectEmployeeType type) {

    @JsonCreator
    public ZepProjectEmployee(Builder builder) {
        this(builder.username, builder.lead, builder.type);
    }

    public static Builder builder() {
        return Builder.aZepProjectEmployee();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        @JsonProperty("employee_id")
        private String username;

        @JsonProperty
        private boolean lead;

        @JsonProperty
        private ZepProjectEmployeeType type;

        public static Builder aZepProjectEmployee() {
            return new Builder();
        }


        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder lead(boolean lead) {
            this.lead = lead;
            return this;
        }

        public Builder type(ZepProjectEmployeeType type) {
            this.type = type;
            return this;
        }

        public ZepProjectEmployee build() {
            return new ZepProjectEmployee(this);
        }

    }
}
