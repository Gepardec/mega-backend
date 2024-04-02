package com.gepardec.mega.domain.model;

import java.time.LocalDate;
import java.util.Map;

public record AbsenceTime (
        String userId,
      LocalDate fromDate,
      LocalDate toDate,
      String reason,
      Boolean accepted
) {
    public AbsenceTime(Builder builder) {
        this(
            builder.userId,
            builder.fromDate,
            builder.toDate,
            builder.reason,
            builder.accepted
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String userId;
        private LocalDate fromDate;
        private LocalDate toDate;
        private String reason;
        private Boolean accepted;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder fromDate(LocalDate fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public Builder toDate(LocalDate toDate) {
            this.toDate = toDate;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder accepted(Boolean accepted) {
            this.accepted = accepted;
            return this;
        }

        public AbsenceTime build() {
            return new AbsenceTime(this);
        }
    }
}
