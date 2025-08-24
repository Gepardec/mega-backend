package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = EmployeeDto.Builder.class)
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

    public final Integer personioId;

    private EmployeeDto(Builder builder) {
        this.userId = builder.userId;
        this.email = builder.email;
        this.title = builder.title;
        this.firstname = builder.firstname;
        this.lastname = builder.lastname;
        this.salutation = builder.salutation;
        this.releaseDate = builder.releaseDate;
        this.workDescription = builder.workDescription;
        this.active = builder.active;
        this.personioId = builder.personioId;
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

    public Integer getPersonioId() {
        return personioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeDto that = (EmployeeDto) o;
        return isActive() == that.isActive() && Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getEmail(), that.getEmail()) && Objects.equals(getTitle(), that.getTitle()) && Objects.equals(getFirstname(), that.getFirstname()) && Objects.equals(getLastname(), that.getLastname()) && Objects.equals(getSalutation(), that.getSalutation()) && Objects.equals(getReleaseDate(), that.getReleaseDate()) && Objects.equals(getWorkDescription(), that.getWorkDescription() ) && Objects.equals(getPersonioId(), that.getPersonioId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getEmail(), getTitle(), getFirstname(), getLastname(), getSalutation(), getReleaseDate(), getWorkDescription(), isActive(), getPersonioId());
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private String userId;
        private String email;
        private String title;
        private String firstname;
        private String lastname;
        private String salutation;
        private String releaseDate;
        private String workDescription;
        private boolean active;
        private Integer personioId;

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

        public Builder personioId(Integer personioId) {
            this.personioId = personioId;
            return this;
        }

        public EmployeeDto build() {
            return new EmployeeDto(this);
        }
    }
}
