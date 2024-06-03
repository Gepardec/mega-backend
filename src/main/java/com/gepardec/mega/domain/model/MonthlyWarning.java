package com.gepardec.mega.domain.model;

import java.util.List;

public class MonthlyWarning {
    private final String name;
    private final List<String> dateValuesWhenWarningsOccurred;

    private MonthlyWarning(Builder builder) {
        this.name = builder.name;
        this.dateValuesWhenWarningsOccurred = builder.dateValuesWhenWarningsOccurred;
    }

    public String getName() {
        return name;
    }

    public List<String> getDateValuesWhenWarningsOccurred() {
        return dateValuesWhenWarningsOccurred;
    }

    public static Builder builder() {return Builder.aMonthlyWarning();}

    public static class Builder {
        private String name;
        private List<String> dateValuesWhenWarningsOccurred;

        private Builder() {}

        public static Builder aMonthlyWarning() {return new Builder();}

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder dateValuesWhenWarningsOccurred(List<String> dateValuesWhenWarningsOccurred) {
            this.dateValuesWhenWarningsOccurred = dateValuesWhenWarningsOccurred;
            return this;
        }

        public MonthlyWarning build() {return new MonthlyWarning(this);}
    }
}
