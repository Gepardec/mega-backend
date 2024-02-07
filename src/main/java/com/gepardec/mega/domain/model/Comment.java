package com.gepardec.mega.domain.model;

import com.gepardec.mega.db.entity.employee.EmployeeState;

public class Comment {

    private final Long id;

    private final String message;

    private final String authorEmail;

    private final String authorName;

    private final String updateDate;

    private final EmployeeState state;

    private final SourceSystem sourceSystem;

    private Comment(Builder builder) {
        this.id = builder.id;
        this.message = builder.message;
        this.authorEmail = builder.authorEmail;
        this.authorName = builder.authorName;
        this.updateDate = builder.updateDate;
        this.state = builder.state;
        this.sourceSystem = builder.sourceSystem;
    }

    public static Builder builder() {
        return Builder.aComment();
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

        public static Builder aComment() {
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

        public Comment build() {
            return new Comment(this);
        }
    }
}
