package com.gepardec.mega.domain.model;

public class ValidationViolation {

    private final String property;

    private final String message;


    public ValidationViolation(Builder builder) {
        this.property = builder.property;
        this.message = builder.message;
    }

    public static Builder builder() {
        return Builder.aValidationViolation();
    }

    public String getProperty() {
        return property;
    }

    public String getMessage() {
        return message;
    }

    public static final class Builder {
        private String property;
        private String message;

        private Builder() {
        }

        public static Builder aValidationViolation() {
            return new Builder();
        }

        public Builder property(String property) {
            this.property = property;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public ValidationViolation build() {
            return new ValidationViolation(this);
        }
    }
}
