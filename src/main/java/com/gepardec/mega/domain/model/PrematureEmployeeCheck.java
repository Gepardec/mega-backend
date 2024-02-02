package com.gepardec.mega.domain.model;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;

import java.time.LocalDate;

public class PrematureEmployeeCheck {
    private final Long id;
    private final User user;
    private final String reason;
    private final LocalDate forMonth;
    private final PrematureEmployeeCheckState state;

    private PrematureEmployeeCheck(Builder builder) {
        this.id = builder.id;
        this.user = builder.user;
        this.reason = builder.reason;
        this.forMonth = builder.forMonth;
        this.state = builder.state;
    }

    public static Builder builder() {
        return Builder.aPrematureEmployeeCheck();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getReason() {
        return reason;
    }

    public LocalDate getForMonth() {
        return forMonth;
    }

    public PrematureEmployeeCheckState getState() {
        return state;
    }

    public static final class Builder {
        private Long id;
        private User user;
        private String reason;
        private LocalDate forMonth;
        private PrematureEmployeeCheckState state;

        private Builder() {
        }

        public static Builder aPrematureEmployeeCheck() {
            return new Builder();
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder forMonth(LocalDate forMonth) {
            this.forMonth = forMonth;
            return this;
        }

        public Builder state(PrematureEmployeeCheckState state) {
            this.state = state;
            return this;
        }

        public PrematureEmployeeCheck build() {
            return new PrematureEmployeeCheck(this);
        }
    }
}
