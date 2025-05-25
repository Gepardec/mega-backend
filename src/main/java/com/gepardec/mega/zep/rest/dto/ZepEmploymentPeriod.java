package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record ZepEmploymentPeriod(LocalDateTime startDate, LocalDateTime endDate) {

    @JsonCreator
    public ZepEmploymentPeriod(Builder builder) {
        this(builder.startDate, builder.endDate);
    }

    public static Builder builder() {
        return Builder.aZepEmploymentPeriod();
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        @JsonProperty("start_date")
        private LocalDateTime startDate;
        @JsonProperty("end_date")
        private LocalDateTime endDate;

        public Builder startDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public static Builder aZepEmploymentPeriod() {
            return new Builder();
        }

        public ZepEmploymentPeriod build() {
            return new ZepEmploymentPeriod(this);
        }
    }
}
