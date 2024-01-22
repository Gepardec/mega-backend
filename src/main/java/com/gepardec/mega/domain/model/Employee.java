package com.gepardec.mega.domain.model;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

public class Employee {

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

    /**
     * Austrittsdatum, wird durch Aufruf von employeeService.getAllEmployeesConsideringExitDate befüllt,
     * wenn Mitarbeiter inaktiv ist.
     */
    private LocalDate exitDate;

    public Employee() {
    }

    public Employee(String userId, String email, String title, String firstname, String lastname, String salutation, String releaseDate, String workDescription, String language, Map<DayOfWeek, Duration> regularWorkingHours, boolean active, LocalDate exitDate) {
        this.userId = userId;
        this.email = email;
        this.title = title;
        this.firstname = firstname;
        this.lastname = lastname;
        this.salutation = salutation;
        this.releaseDate = releaseDate;
        this.workDescription = workDescription;
        this.language = language;
        this.regularWorkingHours = regularWorkingHours;
        this.active = active;
        this.exitDate = exitDate;
    }

    public static EmployeeBuilder builder(){
        return EmployeeBuilder.anEmployee();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {        this.userId = userId;
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Map<DayOfWeek, Duration> getRegularWorkingHours() {
        return regularWorkingHours;
    }

    public void setRegularWorkingHours(Map<DayOfWeek, Duration> regularWorkingHours) {
        this.regularWorkingHours = regularWorkingHours;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getExitDate() {
        return exitDate;
    }

    public void setExitDate(LocalDate exitDate) {
        this.exitDate = exitDate;
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


    public static final class EmployeeBuilder {
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

        private EmployeeBuilder() {
        }

        public static EmployeeBuilder anEmployee() {
            return new EmployeeBuilder();
        }

        public EmployeeBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public EmployeeBuilder email(String email) {
            this.email = email;
            return this;
        }

        public EmployeeBuilder title(String title) {
            this.title = title;
            return this;
        }

        public EmployeeBuilder firstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public EmployeeBuilder lastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public EmployeeBuilder salutation(String salutation) {
            this.salutation = salutation;
            return this;
        }

        public EmployeeBuilder releaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public EmployeeBuilder workDescription(String workDescription) {
            this.workDescription = workDescription;
            return this;
        }

        public EmployeeBuilder language(String language) {
            this.language = language;
            return this;
        }

        public EmployeeBuilder regularWorkingHours(Map<DayOfWeek, Duration> regularWorkingHours) {
            this.regularWorkingHours = regularWorkingHours;
            return this;
        }

        public EmployeeBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public EmployeeBuilder exitDate(LocalDate exitDate) {
            this.exitDate = exitDate;
            return this;
        }

        public Employee build() {
            Employee employee = new Employee();
            employee.setUserId(userId);
            employee.setEmail(email);
            employee.setTitle(title);
            employee.setFirstname(firstname);
            employee.setLastname(lastname);
            employee.setSalutation(salutation);
            employee.setReleaseDate(releaseDate);
            employee.setWorkDescription(workDescription);
            employee.setLanguage(language);
            employee.setRegularWorkingHours(regularWorkingHours);
            employee.setActive(active);
            employee.setExitDate(exitDate);
            return employee;
        }
    }
}
