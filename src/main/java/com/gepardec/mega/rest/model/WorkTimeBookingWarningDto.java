package com.gepardec.mega.rest.model;

import com.gepardec.mega.domain.model.WorkTimeBookingWarning;

import java.util.List;

public class WorkTimeBookingWarningDto {
    private final String name;
    private final List<WorkTimeBookingWarning.WarningDate> warningDates;

    private WorkTimeBookingWarningDto(Builder builder) {
        this.name = builder.name;
        this.warningDates = builder.warningDates;
    }

    public String getName() {
        return name;
    }

    public List<WorkTimeBookingWarning.WarningDate> getWarningDates() {
        return warningDates;
    }

    public static Builder builder() {
        return Builder.aMonthlyWarningDto();
    }

    public static class Builder {
        private String name;
        private List<WorkTimeBookingWarning.WarningDate> warningDates;

        private Builder() {
        }

        public static Builder aMonthlyWarningDto() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder warningDates(List<WorkTimeBookingWarning.WarningDate> warningDates) {
            this.warningDates = warningDates;
            return this;
        }

        public WorkTimeBookingWarningDto build() {
            return new WorkTimeBookingWarningDto(this);
        }
    }
}
