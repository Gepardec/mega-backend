package com.gepardec.mega.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PrematureEmployeeCheck {
    private long id;

    private User user;

    private String reason;

    private LocalDate forMonth;

    private LocalDateTime creationDate;

    public PrematureEmployeeCheck() {
    }

    public PrematureEmployeeCheck(long id, User user, String reason, LocalDate forMonth, LocalDateTime creationDate) {
        this.id = id;
        this.user = user;
        this.reason = reason;
        this.forMonth = forMonth;
        this.creationDate = creationDate;
    }

    public static PrematureEmployeeCheckBuilder builder() {
        return PrematureEmployeeCheckBuilder.aPrematureEmployeeCheck();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getForMonth() {
        return forMonth;
    }

    public void setForMonth(LocalDate forMonth) {
        this.forMonth = forMonth;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public static final class PrematureEmployeeCheckBuilder {
        private long id;
        private User user;
        private String reason;
        private LocalDate forMonth;
        private LocalDateTime creationDate;

        private PrematureEmployeeCheckBuilder() {
        }

        public static PrematureEmployeeCheckBuilder aPrematureEmployeeCheck() {
            return new PrematureEmployeeCheckBuilder();
        }

        public PrematureEmployeeCheckBuilder id(long id) {
            this.id = id;
            return this;
        }

        public PrematureEmployeeCheckBuilder user(User user) {
            this.user = user;
            return this;
        }

        public PrematureEmployeeCheckBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public PrematureEmployeeCheckBuilder forMonth(LocalDate forMonth) {
            this.forMonth = forMonth;
            return this;
        }

        public PrematureEmployeeCheckBuilder creationDate(LocalDateTime creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public PrematureEmployeeCheck build() {
            PrematureEmployeeCheck prematureEmployeeCheck = new PrematureEmployeeCheck();
            prematureEmployeeCheck.setId(id);
            prematureEmployeeCheck.setUser(user);
            prematureEmployeeCheck.setReason(reason);
            prematureEmployeeCheck.setForMonth(forMonth);
            prematureEmployeeCheck.setCreationDate(creationDate);
            return prematureEmployeeCheck;
        }
    }
}
