package com.gepardec.mega.domain.model;

public class Step {
    private long dbId;

    private String name;

    private long ordinal;

    private Role role;

    public Step() {

    }

    public Step(long dbId, String name, long ordinal, Role role) {
        this.dbId = dbId;
        this.name = name;
        this.ordinal = ordinal;
        this.role = role;
    }

    public static StepBuilder builder() {
        return StepBuilder.aStep();
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(long ordinal) {
        this.ordinal = ordinal;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public static final class StepBuilder {
        private long dbId;
        private String name;
        private long ordinal;
        private Role role;

        private StepBuilder() {
        }

        public static StepBuilder aStep() {
            return new StepBuilder();
        }

        public StepBuilder dbId(long dbId) {
            this.dbId = dbId;
            return this;
        }

        public StepBuilder name(String name) {
            this.name = name;
            return this;
        }

        public StepBuilder ordinal(long ordinal) {
            this.ordinal = ordinal;
            return this;
        }

        public StepBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public Step build() {
            Step step = new Step();
            step.setDbId(dbId);
            step.setName(name);
            step.setOrdinal(ordinal);
            step.setRole(role);
            return step;
        }
    }
}



