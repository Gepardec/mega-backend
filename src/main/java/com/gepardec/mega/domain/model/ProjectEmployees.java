package com.gepardec.mega.domain.model;

import java.util.List;

public class ProjectEmployees {
    private String projectId;

    private List<String> employees;

    public ProjectEmployees() {
    }

    public ProjectEmployees(String projectId, List<String> employees) {
        this.projectId = projectId;
        this.employees = employees;
    }

    public static ProjectEmployeesBuilder builder() {
        return ProjectEmployeesBuilder.aProjectEmployees();
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public List<String> getEmployees() {
        return employees;
    }

    public void setEmployees(List<String> employees) {
        this.employees = employees;
    }

    public static final class ProjectEmployeesBuilder {
        private String projectId;
        private List<String> employees;

        private ProjectEmployeesBuilder() {
        }

        public static ProjectEmployeesBuilder aProjectEmployees() {
            return new ProjectEmployeesBuilder();
        }

        public ProjectEmployeesBuilder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public ProjectEmployeesBuilder employees(List<String> employees) {
            this.employees = employees;
            return this;
        }

        public ProjectEmployees build() {
            ProjectEmployees projectEmployees = new ProjectEmployees();
            projectEmployees.setProjectId(projectId);
            projectEmployees.setEmployees(employees);
            return projectEmployees;
        }
    }
}


