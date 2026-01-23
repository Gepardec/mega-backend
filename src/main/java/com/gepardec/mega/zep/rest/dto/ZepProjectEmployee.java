package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ZepProjectEmployee(
        String username,
        LocalDateTime from,
        LocalDateTime to,
        ZepProjectEmployeeType type) {

    @JsonCreator
    public ZepProjectEmployee(Builder builder) {
        this(builder.username, builder.from, builder.to, builder.type);
    }

    public static Builder builder() {
        return Builder.aZepProjectEmployee();
    }

    public boolean isActive(YearMonth payrollMonth) {
        LocalDate monthStart = payrollMonth.atDay(1);
        LocalDate monthEnd = payrollMonth.atEndOfMonth();

        // If both dates are null, employee is active
        if (from == null && to == null) {
            return true;
        }

        LocalDate employeeStart = (from != null) ? from.toLocalDate() : null;
        LocalDate employeeEnd = (to != null) ? to.toLocalDate() : null;

        // Employee starts after month ends
        if (employeeStart != null && employeeStart.isAfter(monthEnd)) {
            return false;
        }

        // Employee ends before month starts
        return employeeEnd == null || !employeeEnd.isBefore(monthStart);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        @JsonProperty("employee_id")
        private String username;

        @JsonProperty
        private LocalDateTime from;

        @JsonProperty
        private LocalDateTime to;

        @JsonProperty
        private ZepProjectEmployeeType type;

        public static Builder aZepProjectEmployee() {
            return new Builder();
        }


        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder from(LocalDateTime from) {
            this.from = from;
            return this;
        }

        public Builder to(LocalDateTime to) {
            this.to = to;
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
