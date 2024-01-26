package com.gepardec.mega.domain.model;

import jakarta.enterprise.inject.Vetoed;

@Vetoed
public class UserContext {
    private final User user;

    private UserContext(Builder builder) {
        this.user = builder.user;
    }

    public static Builder builder() {
        return Builder.anUserContext();
    }

    public User getUser() {
        return user;
    }

    public static final class Builder {
        private User user;

        private Builder() {
        }

        public static Builder anUserContext() {
            return new Builder();
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public UserContext build() {
            return new UserContext(this);
        }
    }
}
