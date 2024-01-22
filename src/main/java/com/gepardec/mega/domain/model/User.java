package com.gepardec.mega.domain.model;


import java.time.LocalDate;
import java.util.Set;

// Represents the logged in user in mega.

public class User {
    private long dbId;

    private String userId;

    private String email;

    private String firstname;

    private String lastname;

    private LocalDate releaseDate;

    private Set<Role> roles;

    public User(long dbId, String userId, String email, String firstname, String lastname, LocalDate releaseDate, Set<Role> roles) {
        this.dbId = dbId;
        this.userId = userId;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.releaseDate = releaseDate;
        this.roles = roles;
    }

    public User() {
    }

    public static UserBuilder builder() {
        return UserBuilder.anUser();
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public static final class UserBuilder {
        private long dbId;
        private String userId;
        private String email;
        private String firstname;
        private String lastname;
        private LocalDate releaseDate;
        private Set<Role> roles;

        private UserBuilder() {
        }

        public static UserBuilder anUser() {
            return new UserBuilder();
        }

        public UserBuilder dbId(long dbId) {
            this.dbId = dbId;
            return this;
        }

        public UserBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder firstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public UserBuilder lastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public UserBuilder releaseDate(LocalDate releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public UserBuilder roles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }

        public User build() {
            User user = new User();
            user.setDbId(dbId);
            user.setUserId(userId);
            user.setEmail(email);
            user.setFirstname(firstname);
            user.setLastname(lastname);
            user.setReleaseDate(releaseDate);
            user.setRoles(roles);
            return user;
        }
    }
}
