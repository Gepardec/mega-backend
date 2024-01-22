package com.gepardec.mega.domain.model;

import com.gepardec.mega.db.entity.employee.EmployeeState;

public class Comment {

    private Long id;

    private String message;

    private String authorEmail;

    private String authorName;

    private String updateDate;

    private EmployeeState state;

    private SourceSystem sourceSystem;

    public Comment() {
    }

    public Comment(Long id, String message, String authorEmail, String authorName, String updateDate, EmployeeState state, SourceSystem sourceSystem) {
        this.id = id;
        this.message = message;
        this.authorEmail = authorEmail;
        this.authorName = authorName;
        this.updateDate = updateDate;
        this.state = state;
        this.sourceSystem = sourceSystem;
    }

    public static CommentBuilder builder() {
        return CommentBuilder.aComment();
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

    public static final class CommentBuilder {
        private Long id;
        private String message;
        private String authorEmail;
        private String authorName;
        private String updateDate;
        private EmployeeState state;
        private SourceSystem sourceSystem;

        private CommentBuilder() {
        }

        public static CommentBuilder aComment() {
            return new CommentBuilder();
        }

        public CommentBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public CommentBuilder message(String message) {
            this.message = message;
            return this;
        }

        public CommentBuilder authorEmail(String authorEmail) {
            this.authorEmail = authorEmail;
            return this;
        }

        public CommentBuilder authorName(String authorName) {
            this.authorName = authorName;
            return this;
        }

        public CommentBuilder updateDate(String updateDate) {
            this.updateDate = updateDate;
            return this;
        }

        public CommentBuilder state(EmployeeState state) {
            this.state = state;
            return this;
        }

        public CommentBuilder sourceSystem(SourceSystem sourceSystem) {
            this.sourceSystem = sourceSystem;
            return this;
        }

        public Comment build() {
            Comment comment = new Comment();
            comment.setId(id);
            comment.setMessage(message);
            comment.setAuthorEmail(authorEmail);
            comment.setAuthorName(authorName);
            comment.setUpdateDate(updateDate);
            comment.setState(state);
            comment.setSourceSystem(sourceSystem);
            return comment;
        }
    }
}
