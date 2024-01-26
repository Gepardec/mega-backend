package com.gepardec.mega.domain.model;

public class Step {

    private final long dbId;

    private final String name;

    private final long ordinal;

    private final Role role;

    public Step(Builder builder) {
        this.dbId = builder.dbId;
        this.name = builder.name;
        this.ordinal = builder.ordinal;
        this.role = builder.role;
    }

    public static Builder builder() {
        return Builder.aStep();
    }

    public long getDbId() {
        return dbId;
    }

    public String getName() {
        return name;
    }

    public long getOrdinal() {
        return ordinal;
    }

    public Role getRole() {
        return role;
    }

    public static final class Builder {
        private long dbId;
        private String name;
        private long ordinal;
        private Role role;

        private Builder() {
        }

        public static Builder aStep() {
            return new Builder();
        }

        public Builder dbId(long dbId) {
            this.dbId = dbId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder ordinal(long ordinal) {
            this.ordinal = ordinal;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Step build() {
           return new Step(this);
        }
    }
}



