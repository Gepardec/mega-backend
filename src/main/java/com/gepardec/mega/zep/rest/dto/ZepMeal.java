package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record ZepMeal(
        Integer id,
        String employeeId,
        OffsetDateTime date,
        boolean breakfast,
        boolean lunch,
        boolean dinner) {

    @JsonCreator
    public ZepMeal(Builder builder) {
        this(builder.id,
             builder.employeeId,
             builder.date,
             builder.breakfast,
             builder.lunch,
             builder.dinner);
    }

    public static Builder builder() {
        return Builder.aZepMeal();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {

        @JsonProperty
        private Integer id;
        @JsonProperty("employee_id")
        private String employeeId;
        @JsonProperty
        private OffsetDateTime date;
        @JsonProperty
        private boolean breakfast;
        @JsonProperty
        private boolean lunch;
        @JsonProperty
        private boolean dinner;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder date(OffsetDateTime date) {
            this.date = date;
            return this;
        }

        public Builder breakfast(boolean breakfast) {
            this.breakfast = breakfast;
            return this;
        }

        public Builder lunch(boolean lunch) {
            this.lunch = lunch;
            return this;
        }

        public Builder dinner(boolean dinner) {
            this.dinner = dinner;
            return this;
        }

        public static Builder aZepMeal() {
            return new Builder();
        }

        public ZepMeal build() {
            return new ZepMeal(this);
        }
    }
}
