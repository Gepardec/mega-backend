package com.gepardec.mega.zep.rest.entity;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ZepRegularWorkingTimes {
        private final int id;
        private final String employee_id;
        private final LocalDateTime start_date;
        private final Double monday;
        private final Double tuesday;
        private final Double wednesday;
        private final Double thursday;
        private final Double friday;
        private final Double saturday;
        private final Double sunday;
        private final Boolean isMonthly;
        private final Double monthlyHours;
        private final Double maxHoursInMonth;
        private final Double maxHoursInWeek;


        @JsonCreator
        public ZepRegularWorkingTimes(Builder builder) {
                this.id = builder.id;
                this.employee_id = builder.employee_id;
                this.start_date = builder.start_date;
                this.monday = builder.monday;
                this.tuesday = builder.tuesday;
                this.wednesday = builder.wednesday;
                this.thursday = builder.thursday;
                this.friday = builder.friday;
                this.saturday = builder.saturday;
                this.sunday = builder.sunday;
                this.isMonthly = builder.isMonthly;
                this.monthlyHours = builder.monthlyHours;
                this.maxHoursInMonth = builder.maxHoursInMonth;
                this.maxHoursInWeek = builder.maxHoursInWeek;
        }

        public int getId() {
                return id;
        }


        public String getEmployee_id() {
                return employee_id;
        }


        public LocalDateTime getStart_date() {
                return start_date;
        }


        public Double getMonday() {
                return monday;
        }


        public Double getTuesday() {
                return tuesday;
        }


        public Double getWednesday() {
                return wednesday;
        }


        public Double getThursday() {
                return thursday;
        }


        public Double getFriday() {
                return friday;
        }


        public Double getSaturday() {
                return saturday;
        }


        public Double getSunday() {
                return sunday;
        }


        public Boolean getIsMonthly() {
                return isMonthly;
        }


        public Double getMonthlyHours() {
                return monthlyHours;
        }


        public Double getMaxHoursInMonth() {
                return maxHoursInMonth;
        }


        public Double getMaxHoursInWeek() {
                return maxHoursInWeek;
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

