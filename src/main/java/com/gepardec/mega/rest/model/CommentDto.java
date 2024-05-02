package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.SourceSystem;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = CommentDto.Builder.class)
public class CommentDto {

    private final Long id;

    private final String message;

    private final String authorEmail;

    private final String authorName;

    private final String updateDate;

    private final EmployeeState state;

    private final SourceSystem sourceSystem;

    private CommentDto(Builder builder) {
        this.id = builder.id;
        this.message = builder.message;
        this.authorEmail = builder.authorEmail;
        this.authorName = builder.authorName;
        this.updateDate = builder.updateDate;
        this.state = builder.state;
        this.sourceSystem = builder.sourceSystem;
    }

    public static Builder builder() {
        return Builder.aCommentDto();
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public EmployeeState getState() {
        return state;
    }

    public SourceSystem getSourceSystem() {
        return sourceSystem;
    }


    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private Long id;
        private String message;
        private String authorEmail;
        private String authorName;
        private String updateDate;
        private EmployeeState state;
        private SourceSystem sourceSystem;

        private Builder() {
        }

        public static Builder aCommentDto() {
            return new Builder();
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder authorEmail(String authorEmail) {
            this.authorEmail = authorEmail;
            return this;
        }

        public Builder authorName(String authorName) {
            this.authorName = authorName;
            return this;
        }

        public Builder updateDate(String updateDate) {
            this.updateDate = updateDate;
            return this;
        }

        public Builder state(EmployeeState state) {
            this.state = state;
            return this;
        }

        public Builder sourceSystem(SourceSystem sourceSystem) {
            this.sourceSystem = sourceSystem;
            return this;
        }

        public CommentDto build() {
            return new CommentDto(this);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentDto that = (CommentDto) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getMessage(), that.getMessage()) && Objects.equals(getAuthorEmail(), that.getAuthorEmail()) && Objects.equals(getAuthorName(), that.getAuthorName()) && Objects.equals(getUpdateDate(), that.getUpdateDate()) && getState() == that.getState() && getSourceSystem() == that.getSourceSystem();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getMessage(), getAuthorEmail(), getAuthorName(), getUpdateDate(), getState(), getSourceSystem());
    }
}
