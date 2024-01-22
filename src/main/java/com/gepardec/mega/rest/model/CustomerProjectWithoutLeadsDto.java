package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerProjectWithoutLeadsDto {

    private String projectName;
    private String comment;
    private LocalDate fetchDate;
    private Integer zepId;


    public CustomerProjectWithoutLeadsDto() {
    }

    public CustomerProjectWithoutLeadsDto(String projectName, String comment, LocalDate fetchDate, Integer zepId) {
        this.projectName = projectName;
        this.comment = comment;
        this.fetchDate = fetchDate;
        this.zepId = zepId;
    }

    public static CustomerProjectWithoutLeadsDtoBuilder builder() {
        return CustomerProjectWithoutLeadsDtoBuilder.aCustomerProjectWithoutLeadsDto();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerProjectWithoutLeadsDto that = (CustomerProjectWithoutLeadsDto) o;
        return Objects.equals(getProjectName(), that.getProjectName()) && Objects.equals(getComment(), that.getComment()) && Objects.equals(getFetchDate(), that.getFetchDate()) && Objects.equals(getZepId(), that.getZepId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProjectName(), getComment(), getFetchDate(), getZepId());
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getFetchDate() {
        return fetchDate;
    }

    public void setFetchDate(LocalDate fetchDate) {
        this.fetchDate = fetchDate;
    }

    public Integer getZepId() {
        return zepId;
    }

    public void setZepId(Integer zepId) {
        this.zepId = zepId;
    }

    public static final class CustomerProjectWithoutLeadsDtoBuilder {
        private String projectName;
        private String comment;
        private LocalDate fetchDate;
        private Integer zepId;

        private CustomerProjectWithoutLeadsDtoBuilder() {
        }

        public static CustomerProjectWithoutLeadsDtoBuilder aCustomerProjectWithoutLeadsDto() {
            return new CustomerProjectWithoutLeadsDtoBuilder();
        }

        public CustomerProjectWithoutLeadsDtoBuilder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public CustomerProjectWithoutLeadsDtoBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public CustomerProjectWithoutLeadsDtoBuilder fetchDate(LocalDate fetchDate) {
            this.fetchDate = fetchDate;
            return this;
        }

        public CustomerProjectWithoutLeadsDtoBuilder zepId(Integer zepId) {
            this.zepId = zepId;
            return this;
        }

        public CustomerProjectWithoutLeadsDto build() {
            CustomerProjectWithoutLeadsDto customerProjectWithoutLeadsDto = new CustomerProjectWithoutLeadsDto();
            customerProjectWithoutLeadsDto.setProjectName(projectName);
            customerProjectWithoutLeadsDto.setComment(comment);
            customerProjectWithoutLeadsDto.setFetchDate(fetchDate);
            customerProjectWithoutLeadsDto.setZepId(zepId);
            return customerProjectWithoutLeadsDto;
        }
    }
}
