package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeDto {

    private final String userId;

    private final String email;

    private final String title;

    private final String firstname;

    private final String lastname;

    private final String salutation;

    private final String releaseDate;

    private final String workDescription;

    private final boolean active;

    @JsonCreator
    public EmployeeDto(Builder builder) {
        this.userId = builder.userId;
        this.email = builder.email;
        this.title = builder.title;
        this.firstname = builder.firstname;
        this.lastname = builder.lastname;
        this.salutation = builder.salutation;
        this.releaseDate = builder.releaseDate;
        this.workDescription = builder.workDescription;
        this.active = builder.active;
    }

    public static Builder builder() {
        return Builder.anEmployeeDto();
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

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeDto that = (EmployeeDto) o;
        return isActive() == that.isActive() && Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getEmail(), that.getEmail()) && Objects.equals(getTitle(), that.getTitle()) && Objects.equals(getFirstname(), that.getFirstname()) && Objects.equals(getLastname(), that.getLastname()) && Objects.equals(getSalutation(), that.getSalutation()) && Objects.equals(getReleaseDate(), that.getReleaseDate()) && Objects.equals(getWorkDescription(), that.getWorkDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getEmail(), getTitle(), getFirstname(), getLastname(), getSalutation(), getReleaseDate(), getWorkDescription(), isActive());
    }

    public static final class Builder {
        @JsonProperty private String userId;
        @JsonProperty private String email;
        @JsonProperty private String title;
        @JsonProperty private String firstname;
        @JsonProperty private String lastname;
        @JsonProperty private String salutation;
        @JsonProperty private String releaseDate;
        @JsonProperty private String workDescription;
        @JsonProperty private boolean active;

        private Builder() {
        }

        public static Builder anEmployeeDto() {
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

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public EmployeeDto build() {
            return new EmployeeDto(this);
        }
    }
}
