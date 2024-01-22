package com.gepardec.mega.domain.model;

import java.time.LocalDate;
import java.util.List;

public class Project {
    private Integer zepId;

    private String projectId;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private List<String> employees;

    private List<String> leads;

    private List<String> categories;

    private BillabilityPreset billabilityPreset;

    public boolean isBillable() {
        return BillabilityPreset.isBillable(billabilityPreset);
    }

    public Project() {
    }

    public Project(Integer zepId, String projectId, String description, LocalDate startDate, LocalDate endDate, List<String> employees, List<String> leads, List<String> categories, BillabilityPreset billabilityPreset) {
        this.zepId = zepId;
        this.projectId = projectId;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.employees = employees;
        this.leads = leads;
        this.categories = categories;
        this.billabilityPreset = billabilityPreset;
    }

    public static ProjectBuilder builder() {
        return ProjectBuilder.aProject();
    }

    public Integer getZepId() {
        return zepId;
    }

    public void setZepId(Integer zepId) {
        this.zepId = zepId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<String> getEmployees() {
        return employees;
    }

    public void setEmployees(List<String> employees) {
        this.employees = employees;
    }

    public List<String> getLeads() {
        return leads;
    }

    public void setLeads(List<String> leads) {
        this.leads = leads;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public BillabilityPreset getBillabilityPreset() {
        return billabilityPreset;
    }

    public void setBillabilityPreset(BillabilityPreset billabilityPreset) {
        this.billabilityPreset = billabilityPreset;
    }

    public static final class ProjectBuilder {
        private Integer zepId;
        private String projectId;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<String> employees;
        private List<String> leads;
        private List<String> categories;
        private BillabilityPreset billabilityPreset;

        private ProjectBuilder() {
        }

        public static ProjectBuilder aProject() {
            return new ProjectBuilder();
        }

        public ProjectBuilder zepId(Integer zepId) {
            this.zepId = zepId;
            return this;
        }

        public ProjectBuilder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public ProjectBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ProjectBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public ProjectBuilder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public ProjectBuilder employees(List<String> employees) {
            this.employees = employees;
            return this;
        }

        public ProjectBuilder leads(List<String> leads) {
            this.leads = leads;
            return this;
        }

        public ProjectBuilder categories(List<String> categories) {
            this.categories = categories;
            return this;
        }

        public ProjectBuilder billabilityPreset(BillabilityPreset billabilityPreset) {
            this.billabilityPreset = billabilityPreset;
            return this;
        }

        public Project build() {
            Project project = new Project();
            project.setZepId(zepId);
            project.setProjectId(projectId);
            project.setDescription(description);
            project.setStartDate(startDate);
            project.setEndDate(endDate);
            project.setEmployees(employees);
            project.setLeads(leads);
            project.setCategories(categories);
            project.setBillabilityPreset(billabilityPreset);
            return project;
        }
    }
}
