package com.gepardec.mega.domain.model;

import jakarta.enterprise.inject.Vetoed;

@Vetoed
public class UserContext {
    private User user;

    private UserContext() {
    }

    public UserContext(User user) {
        this.user = user;
    }

    public static UserContextBuilder builder() {
        return UserContextBuilder.anUserContext();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static final class UserContextBuilder {
        private User user;

        private UserContextBuilder() {
        }

        public static UserContextBuilder anUserContext() {
            return new UserContextBuilder();
        }

        public UserContextBuilder user(User user) {
            this.user = user;
            return this;
        }

        public UserContext build() {
            UserContext userContext = new UserContext();
            userContext.setUser(user);
            return userContext;
        }
    }
}
