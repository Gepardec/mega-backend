package com.gepardec.mega.domain.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Employee model (mutable wegen employmentPeriods und regularWorkingTimes)
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

    private EmploymentPeriods employmentPeriods;

    private RegularWorkingTimes regularWorkingTimes;

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
        this.employmentPeriods = Optional.ofNullable(builder.employmentPeriods).orElse(EmploymentPeriods.empty());
        this.regularWorkingTimes = Optional.ofNullable(builder.regularWorkingTimes).orElse(RegularWorkingTimes.empty());
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

    public EmploymentPeriods getEmploymentPeriods() {
        return employmentPeriods;
    }

    public void setEmploymentPeriods(EmploymentPeriods employmentPeriods) {
        this.employmentPeriods = employmentPeriods;
    }

    public RegularWorkingTimes getRegularWorkingTimes() {
        return regularWorkingTimes;
    }

    public void setRegularWorkingTimes(RegularWorkingTimes regularWorkingTimes) {
        this.regularWorkingTimes = regularWorkingTimes;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Employee employee = (Employee) o;
        return Objects.equals(getUserId(), employee.getUserId()) && Objects.equals(getEmail(), employee.getEmail()) && Objects.equals(getTitle(), employee.getTitle()) && Objects.equals(getFirstname(), employee.getFirstname()) && Objects.equals(getLastname(), employee.getLastname()) && Objects.equals(getSalutation(), employee.getSalutation()) && Objects.equals(getReleaseDate(), employee.getReleaseDate()) && Objects.equals(getWorkDescription(), employee.getWorkDescription()) && Objects.equals(getLanguage(), employee.getLanguage()) && Objects.equals(getEmploymentPeriods(), employee.getEmploymentPeriods()) && Objects.equals(getRegularWorkingTimes(), employee.getRegularWorkingTimes());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getUserId());
        result = 31 * result + Objects.hashCode(getEmail());
        result = 31 * result + Objects.hashCode(getTitle());
        result = 31 * result + Objects.hashCode(getFirstname());
        result = 31 * result + Objects.hashCode(getLastname());
        result = 31 * result + Objects.hashCode(getSalutation());
        result = 31 * result + Objects.hashCode(getReleaseDate());
        result = 31 * result + Objects.hashCode(getWorkDescription());
        result = 31 * result + Objects.hashCode(getLanguage());
        result = 31 * result + Objects.hashCode(getEmploymentPeriods());
        result = 31 * result + Objects.hashCode(getRegularWorkingTimes());
        return result;
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
        private EmploymentPeriods employmentPeriods;
        private RegularWorkingTimes regularWorkingTimes;

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

        public Builder workDescription(String workDescription) {
            this.workDescription = workDescription;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder employmentPeriods(EmploymentPeriods employmentPeriods) {
            this.employmentPeriods = employmentPeriods;
            return this;
        }

        public Builder regularWorkingTimes(RegularWorkingTimes regularWorkingTimes) {
            this.regularWorkingTimes = regularWorkingTimes;
            return this;
        }

        public Employee build() {
            return new Employee(this);
        }
    }
}
