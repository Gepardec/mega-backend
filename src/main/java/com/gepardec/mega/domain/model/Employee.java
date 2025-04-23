package com.gepardec.mega.domain.model;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

/**
 * Employee model (mutable wegen exitDate)
 */
public class Employee {

    private final String userId;

    private final String email;

    private final String title;

    private final String firstname;

    private final String lastname;

    private final String salutation;

    private final String releaseDate;

    private final String workDescription;

    private final String language;

    private Map<DayOfWeek, Duration> regularWorkingHours;

    private boolean active;

    private final LocalDate firstDayCurrentEmploymentPeriod;
    /**
     * Austrittsdatum, wird durch Aufruf von employeeService.getAllEmployeesConsideringExitDate befüllt,
     * wenn Mitarbeiter inaktiv ist.
     */
    private LocalDate exitDate;

    private Employee(Builder builder) {
        this.userId = builder.userId;
        this.email = builder.email;
        this.title = builder.title;
        this.firstname = builder.firstname;
        this.lastname = builder.lastname;
        this.salutation = builder.salutation;
        this.releaseDate = builder.releaseDate;
        this.workDescription = builder.workDescription;
        this.language = builder.language;
        this.regularWorkingHours = builder.regularWorkingHours;
        this.active = builder.active;
        this.exitDate = builder.exitDate;
        this.firstDayCurrentEmploymentPeriod = builder.firstDayCurrentEmploymentPeriod;
    }

    public static Builder builder() {
        return Builder.anEmployee();
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }


    public String getSalutation() {
        return salutation;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getWorkDescription() {
        return workDescription;
    }

    public String getLanguage() {
        return language;
    }

    public Map<DayOfWeek, Duration> getRegularWorkingHours() {
        return regularWorkingHours;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setRegularWorkingHours(Map<DayOfWeek, Duration> regularWorkingHours) {
        this.regularWorkingHours = regularWorkingHours;
    }

    public LocalDate getExitDate() {
        return exitDate;
    }

    public void setExitDate(LocalDate exitDate) {
        this.exitDate = exitDate;
    }

    public LocalDate getFirstDayCurrentEmploymentPeriod() {
        return firstDayCurrentEmploymentPeriod;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return isActive() == employee.isActive() && Objects.equals(getUserId(), employee.getUserId()) && Objects.equals(getEmail(), employee.getEmail()) && Objects.equals(getTitle(), employee.getTitle()) && Objects.equals(getFirstname(), employee.getFirstname()) && Objects.equals(getLastname(), employee.getLastname()) && Objects.equals(getSalutation(), employee.getSalutation()) && Objects.equals(getReleaseDate(), employee.getReleaseDate()) && Objects.equals(getWorkDescription(), employee.getWorkDescription()) && Objects.equals(getLanguage(), employee.getLanguage()) && Objects.equals(getRegularWorkingHours(), employee.getRegularWorkingHours()) && Objects.equals(getExitDate(), employee.getExitDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getEmail(), getTitle(), getFirstname(), getLastname(), getSalutation(), getReleaseDate(), getWorkDescription(), getLanguage(), getRegularWorkingHours(), isActive(), getExitDate());
    }


    public static final class Builder {
        private String userId;
        private String email;
        private String title;
        private String firstname;
        private String lastname;
        private String salutation;
        private String releaseDate;
        private String workDescription;
        private String language;
        private Map<DayOfWeek, Duration> regularWorkingHours;
        private boolean active;
        private LocalDate exitDate;
        private LocalDate firstDayCurrentEmploymentPeriod;

        private Builder() {
        }

        public static Builder anEmployee() {
            return new Builder();
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder firstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public Builder lastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public Builder salutation(String salutation) {
            this.salutation = salutation;
            return this;
        }

        public Builder releaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public Builder firstDayCurrentEmploymentPeriod(LocalDate firstDayCurrentEmploymentPeriod) {
            this.firstDayCurrentEmploymentPeriod = firstDayCurrentEmploymentPeriod;
            return this;
        }

        public Builder workDescription(String workDescription) {
            this.workDescription = workDescription;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder regularWorkingHours(Map<DayOfWeek, Duration> regularWorkingHours) {
            this.regularWorkingHours = regularWorkingHours;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder exitDate(LocalDate exitDate) {
            this.exitDate = exitDate;
            return this;
        }

        public Employee build() {
            return new Employee(this);
        }
    }
}
