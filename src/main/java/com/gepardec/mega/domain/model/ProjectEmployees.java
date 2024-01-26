package com.gepardec.mega.domain.model;

import java.util.List;

public class ProjectEmployees {
    private final String projectId;

    private final List<String> employees;


    public ProjectEmployees(Builder builder) {
        this.projectId = builder.projectId;
        this.employees = builder.employees;
    }

    public static Builder builder() {
        return Builder.aProjectEmployees();
    }

    public String getProjectId() {
        return projectId;
    }

    public List<String> getEmployees() {
        return employees;
    }

    public static final class Builder {
        private String projectId;
        private List<String> employees;

        private Builder() {
        }

        public static Builder aProjectEmployees() {
            return new Builder();
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder employees(List<String> employees) {
            this.employees = employees;
            return this;
        }

        public ProjectEmployees build() {
           return new ProjectEmployees(this);
        }
    }
}


