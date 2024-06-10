package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)

public record ZepProjectEmployee (
        String username,
        ZepProjectEmployeeType type) {

    @JsonCreator
    public ZepProjectEmployee(Builder builder) {
        this(builder.username, builder.type);
    }

    public static Builder builder() {
        return Builder.aZepProjectEmployee();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        @JsonProperty("employee_id")
        private String username;

        @JsonProperty
        private ZepProjectEmployeeType type;

        public static Builder aZepProjectEmployee() {
            return new Builder();
        }


        public Builder username(String username) {
            this.username = username;
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
