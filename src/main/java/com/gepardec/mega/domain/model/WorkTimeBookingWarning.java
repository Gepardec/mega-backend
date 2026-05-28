package com.gepardec.mega.domain.model;

import java.util.List;

public class WorkTimeBookingWarning {
    private final String name;
    private final List<WarningDate> warningDates;

    private WorkTimeBookingWarning(Builder builder) {
        this.name = builder.name;
        this.warningDates = builder.warningDates;
    }

    public String getName() {
        return name;
    }

    public List<WarningDate> getWarningDates() {
        return warningDates;
    }

    public static Builder builder() {
        return Builder.aMonthlyWarning();
    }

    public static class Builder {
        private String name;
        private List<WarningDate> warningDates;

        private Builder() {
        }

        public static Builder aMonthlyWarning() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder warningDates(List<WarningDate> warningDates) {
            this.warningDates = warningDates;
            return this;
        }

        public WorkTimeBookingWarning build() {
            return new WorkTimeBookingWarning(this);
        }
    }

    public record WarningDate(String date, Double hours) {
    }
}
