package com.gepardec.mega.domain.model;

import java.time.LocalDate;
import java.util.List;

public class Project {
    private final Integer zepId;

    private final String projectId;

    private final String description;

    private final LocalDate startDate;

    private final LocalDate endDate;

    private final List<String> employees;

    private final List<String> leads;

    private final List<String> categories;

    private final BillabilityPreset billabilityPreset;

    private Project(Builder builder) {
        this.zepId = builder.zepId;
        this.projectId = builder.projectId;
        this.description = builder.description;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.employees = builder.employees;
        this.leads = builder.leads;
        this.categories = builder.categories;
        this.billabilityPreset = builder.billabilityPreset;
    }

    public boolean isBillable() {
        return BillabilityPreset.isBillable(billabilityPreset);
    }

    public static Builder builder() {
        return Builder.aProject();
    }

    public Integer getZepId() {
        return zepId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<String> getEmployees() {
        return employees;
    }

    public List<String> getLeads() {
        return leads;
    }

    public List<String> getCategories() {
        return categories;
    }

    public BillabilityPreset getBillabilityPreset() {
        return billabilityPreset;
    }

    public static final class Builder {
        private Integer zepId;
        private String projectId;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<String> employees;
        private List<String> leads;
        private List<String> categories;
        private BillabilityPreset billabilityPreset;

        private Builder() {
        }

        public static Builder aProject() {
            return new Builder();
        }

        public Builder zepId(Integer zepId) {
            this.zepId = zepId;
            return this;
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder employees(List<String> employees) {
            this.employees = employees;
            return this;
        }

        public Builder leads(List<String> leads) {
            this.leads = leads;
            return this;
        }

        public Builder categories(List<String> categories) {
            this.categories = categories;
            return this;
        }

        public Builder billabilityPreset(BillabilityPreset billabilityPreset) {
            this.billabilityPreset = billabilityPreset;
            return this;
        }

        public Project build() {
            return new Project(this);
        }
    }
}
