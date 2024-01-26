package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerProjectWithoutLeadsDto {

    private final String projectName;
    private final String comment;
    private final LocalDate fetchDate;
    private final Integer zepId;


    @JsonCreator
    public CustomerProjectWithoutLeadsDto(Builder builder) {
        this.projectName = builder.projectName;
        this.comment = builder.comment;
        this.fetchDate = builder.fetchDate;
        this.zepId = builder.zepId;
    }

    public static Builder builder() {
        return Builder.aCustomerProjectWithoutLeadsDto();
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

    public String getComment() {
        return comment;
    }

    public LocalDate getFetchDate() {
        return fetchDate;
    }

    public Integer getZepId() {
        return zepId;
    }

    public static final class Builder {
        @JsonProperty private String projectName;
        @JsonProperty private String comment;
        @JsonProperty private LocalDate fetchDate;
        @JsonProperty private Integer zepId;

        private Builder() {
        }

        public static Builder aCustomerProjectWithoutLeadsDto() {
            return new Builder();
        }

        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder fetchDate(LocalDate fetchDate) {
            this.fetchDate = fetchDate;
            return this;
        }

        public Builder zepId(Integer zepId) {
            this.zepId = zepId;
            return this;
        }

        public CustomerProjectWithoutLeadsDto build() {
          return new CustomerProjectWithoutLeadsDto(this);
        }
    }
}
