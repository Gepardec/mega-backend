package com.gepardec.mega.zep.rest.dto;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

//generate all record fields
public record ZepRegularWorkingTimes (
        LocalDateTime startDate,
        Double monday,
        Double tuesday,
        Double wednesday,
        Double thursday,
        Double friday,
        Double saturday,
        Double sunday) {


        @JsonCreator
        public ZepRegularWorkingTimes(Builder builder) {
                this(
                        builder.startDate,
                        builder.monday,
                        builder.tuesday,
                        builder.wednesday,
                        builder.thursday,
                        builder.friday,
                        builder.saturday,
                        builder.sunday
                );
        }


        public static Builder builder() {
                return Builder.aZepRegularWorkingTimes();
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Builder {
                @JsonProperty("start_date")
                private LocalDateTime startDate;
                @JsonProperty
                private Double monday;
                @JsonProperty
                private Double tuesday;
                @JsonProperty
                private Double wednesday;
                @JsonProperty
                private Double thursday;
                @JsonProperty
                private Double friday;
                @JsonProperty
                private Double saturday;
                @JsonProperty
                private Double sunday;


                public Builder startDate(LocalDateTime startDate) {
                        this.startDate = startDate;
                        return this;
                }

                public Builder monday(Double monday) {
                        this.monday = monday;
                        return this;
                }

                public Builder tuesday(Double tuesday) {
                        this.tuesday = tuesday;
                        return this;
                }

                public Builder wednesday(Double wednesday) {
                        this.wednesday = wednesday;
                        return this;
                }

                public Builder thursday(Double thursday) {
                        this.thursday = thursday;
                        return this;
                }

                public Builder friday(Double friday) {
                        this.friday = friday;
                        return this;
                }

                public Builder saturday(Double saturday) {
                        this.saturday = saturday;
                        return this;
                }

                public Builder sunday(Double sunday) {
                        this.sunday = sunday;
                        return this;
                }

                public ZepRegularWorkingTimes build() {
                        return new ZepRegularWorkingTimes(this);
                }

                public static Builder aZepRegularWorkingTimes() {
                        return new Builder();
                }
        }

}

