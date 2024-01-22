package com.gepardec.mega.domain.model;

public class ValidationViolation {

    private String property;

    private String message;

    public ValidationViolation() {
    }

    public ValidationViolation(String property, String message) {
        this.property = property;
        this.message = message;
    }

    public static ValidationViolationBuilder builder() {
        return ValidationViolationBuilder.aValidationViolation();
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static final class ValidationViolationBuilder {
        private String property;
        private String message;

        private ValidationViolationBuilder() {
        }

        public static ValidationViolationBuilder aValidationViolation() {
            return new ValidationViolationBuilder();
        }

        public ValidationViolationBuilder property(String property) {
            this.property = property;
            return this;
        }

        public ValidationViolationBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ValidationViolation build() {
            ValidationViolation validationViolation = new ValidationViolation();
            validationViolation.setProperty(property);
            validationViolation.setMessage(message);
            return validationViolation;
        }
    }
}
