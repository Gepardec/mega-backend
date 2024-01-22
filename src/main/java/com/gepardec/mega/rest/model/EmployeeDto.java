package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeDto {
    @JsonProperty
    private String userId;

    @JsonProperty
    private String email;

    @JsonProperty
    private String title;

    @JsonProperty
    private String firstname;

    @JsonProperty
    private String lastname;

    @JsonProperty
    private String salutation;

    @JsonProperty
    private String releaseDate;

    @JsonProperty
    private String workDescription;

    @JsonProperty
    private boolean active;

    public EmployeeDto() {
    }

    public EmployeeDto(String userId, String email, String title, String firstname, String lastname, String salutation, String releaseDate, String workDescription, boolean active) {
        this.userId = userId;
        this.email = email;
        this.title = title;
        this.firstname = firstname;
        this.lastname = lastname;
        this.salutation = salutation;
        this.releaseDate = releaseDate;
        this.workDescription = workDescription;
        this.active = active;
    }

    public static EmployeeDtoBuilder builder() {
        return EmployeeDtoBuilder.anEmployeeDto();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getWorkDescription() {
        return workDescription;
    }

    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public static final class EmployeeDtoBuilder {
        private String userId;
        private String email;
        private String title;
        private String firstname;
        private String lastname;
        private String salutation;
        private String releaseDate;
        private String workDescription;
        private boolean active;

        private EmployeeDtoBuilder() {
        }

        public static EmployeeDtoBuilder anEmployeeDto() {
            return new EmployeeDtoBuilder();
        }

        public EmployeeDtoBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public EmployeeDtoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public EmployeeDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public EmployeeDtoBuilder firstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public EmployeeDtoBuilder lastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public EmployeeDtoBuilder salutation(String salutation) {
            this.salutation = salutation;
            return this;
        }

        public EmployeeDtoBuilder releaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public EmployeeDtoBuilder workDescription(String workDescription) {
            this.workDescription = workDescription;
            return this;
        }

        public EmployeeDtoBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public EmployeeDto build() {
            EmployeeDto employeeDto = new EmployeeDto();
            employeeDto.setUserId(userId);
            employeeDto.setEmail(email);
            employeeDto.setTitle(title);
            employeeDto.setFirstname(firstname);
            employeeDto.setLastname(lastname);
            employeeDto.setSalutation(salutation);
            employeeDto.setReleaseDate(releaseDate);
            employeeDto.setWorkDescription(workDescription);
            employeeDto.setActive(active);
            return employeeDto;
        }
    }
}
