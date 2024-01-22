package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.SourceSystem;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentDto {

    @JsonProperty
    private Long id;

    @JsonProperty
    private String message;

    @JsonProperty
    private String authorEmail;

    @JsonProperty
    private String authorName;

    @JsonProperty
    private String updateDate;

    @JsonProperty
    private EmployeeState state;

    @JsonProperty
    private SourceSystem sourceSystem;

    public CommentDto() {
    }

    public CommentDto(Long id, String message, String authorEmail, String authorName, String updateDate, EmployeeState state, SourceSystem sourceSystem) {
        this.id = id;
        this.message = message;
        this.authorEmail = authorEmail;
        this.authorName = authorName;
        this.updateDate = updateDate;
        this.state = state;
        this.sourceSystem = sourceSystem;
    }

    public static CommentDtoBuilder builder() {
        return CommentDtoBuilder.aCommentDto();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public EmployeeState getState() {
        return state;
    }

    public void setState(EmployeeState state) {
        this.state = state;
    }

    public SourceSystem getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(SourceSystem sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public static final class CommentDtoBuilder {
        private Long id;
        private String message;
        private String authorEmail;
        private String authorName;
        private String updateDate;
        private EmployeeState state;
        private SourceSystem sourceSystem;

        private CommentDtoBuilder() {
        }

        public static CommentDtoBuilder aCommentDto() {
            return new CommentDtoBuilder();
        }

        public CommentDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public CommentDtoBuilder message(String message) {
            this.message = message;
            return this;
        }

        public CommentDtoBuilder authorEmail(String authorEmail) {
            this.authorEmail = authorEmail;
            return this;
        }

        public CommentDtoBuilder authorName(String authorName) {
            this.authorName = authorName;
            return this;
        }

        public CommentDtoBuilder updateDate(String updateDate) {
            this.updateDate = updateDate;
            return this;
        }

        public CommentDtoBuilder state(EmployeeState state) {
            this.state = state;
            return this;
        }

        public CommentDtoBuilder sourceSystem(SourceSystem sourceSystem) {
            this.sourceSystem = sourceSystem;
            return this;
        }

        public CommentDto build() {
            CommentDto commentDto = new CommentDto();
            commentDto.setId(id);
            commentDto.setMessage(message);
            commentDto.setAuthorEmail(authorEmail);
            commentDto.setAuthorName(authorName);
            commentDto.setUpdateDate(updateDate);
            commentDto.setState(state);
            commentDto.setSourceSystem(sourceSystem);
            return commentDto;
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
