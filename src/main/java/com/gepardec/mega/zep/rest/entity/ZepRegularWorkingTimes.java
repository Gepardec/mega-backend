package com.gepardec.mega.zep.rest.entity;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

//generate all record fields
public record ZepRegularWorkingTimes (
        int id,
        String employeeId,
        LocalDateTime startDate,
        Double monday,
        Double tuesday,
        Double wednesday,
        Double thursday,
        Double friday,
        Double saturday,
        Double sunday,
        Boolean isMonthly,
        Double monthlyHours,
        Double maxHoursInMonth,
        Double maxHoursInWeek) {


        @JsonCreator
        public ZepRegularWorkingTimes(Builder builder) {
                this(builder.id,
                        builder.employee_id,
                        builder.start_date,
                        builder.monday,
                        builder.tuesday,
                        builder.wednesday,
                        builder.thursday,
                        builder.friday,
                        builder.saturday,
                        builder.sunday,
                        builder.isMonthly,
                        builder.monthlyHours,
                        builder.maxHoursInMonth,
                        builder.maxHoursInWeek
                );
        }


        public static Builder builder() {
                return Builder.aZepRegularWorkingTimes();
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Builder {
                @JsonProperty
                private int id;
                @JsonProperty
                private String employee_id;
                @JsonProperty
                private LocalDateTime start_date;
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

                @JsonProperty("is_monthly")
                private Boolean isMonthly;

                @JsonProperty("monthly_hours")
                private Double monthlyHours;

                @JsonProperty("max_hours_in_month")
                private Double maxHoursInMonth;

                @JsonProperty("max_hours_in_week")
                private Double maxHoursInWeek;

                public Builder id(int id) {
                        this.id = id;
                        return this;
                }

                public Builder employee_id(String employee_id) {
                        this.employee_id = employee_id;
                        return this;
                }

                public Builder start_date(LocalDateTime start_date) {
                        this.start_date = start_date;
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

                public Builder isMonthly(Boolean isMonthly) {
                        this.isMonthly = isMonthly;
                        return this;
                }

                public Builder monthlyHours(Double monthlyHours) {
                        this.monthlyHours = monthlyHours;
                        return this;
                }

                public Builder maxHoursInMonth(Double maxHoursInMonth) {
                        this.maxHoursInMonth = maxHoursInMonth;
                        return this;
                }

                public Builder maxHoursInWeek(Double maxHoursInWeek) {
                        this.maxHoursInWeek = maxHoursInWeek;
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

