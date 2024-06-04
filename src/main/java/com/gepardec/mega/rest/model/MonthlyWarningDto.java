package com.gepardec.mega.rest.model;

import java.util.List;

public class MonthlyWarningDto {
    private final String name;
    private final List<String> datesWhenWarningOccurred;

    private MonthlyWarningDto(Builder builder) {
        this.name = builder.name;
        this.datesWhenWarningOccurred = builder.datesWhenWarningOccurred;
    }

    public String getName() {
        return name;
    }

    public List<String> getDatesWhenWarningOccurred() {
        return datesWhenWarningOccurred;
    }

    public static Builder builder() {return Builder.aMonthlyWarningDto();}

    public static class Builder {
        private String name;
        private List<String> datesWhenWarningOccurred;

        private Builder() {}

        public static Builder aMonthlyWarningDto() {return new Builder();}

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder datesWhenWarningOccurred(List<String> datesWhenWarningOccurred) {
            this.datesWhenWarningOccurred = datesWhenWarningOccurred;
            return this;
        }

        public MonthlyWarningDto build() {return new MonthlyWarningDto(this);}
    }
}