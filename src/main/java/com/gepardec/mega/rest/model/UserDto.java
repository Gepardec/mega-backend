package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.domain.model.Role;

import java.time.LocalDate;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

    private final long dbId;

    private final String userId;

    private final String email;

    private final String firstname;

    private final String lastname;

    private final LocalDate releaseDate;

    private final Set<Role> roles;

    @JsonCreator
    private UserDto(Builder builder) {
        this.dbId = builder.dbId;
        this.userId = builder.userId;
        this.email = builder.email;
        this.firstname = builder.firstname;
        this.lastname = builder.lastname;
        this.releaseDate = builder.releaseDate;
        this.roles = builder.roles;
    }

    public static Builder builder() {
        return Builder.anUserDto();
    }

    public long getDbId() {
        return dbId;
    }


    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public static final class Builder {
        @JsonProperty
        private long dbId;
        @JsonProperty
        private String userId;
        @JsonProperty
        private String email;
        @JsonProperty
        private String firstname;
        @JsonProperty
        private String lastname;
        @JsonProperty
        private LocalDate releaseDate;
        @JsonProperty
        private Set<Role> roles;

        private Builder() {
        }

        public static Builder anUserDto() {
            return new Builder();
        }

        public Builder dbId(long dbId) {
            this.dbId = dbId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder firstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public Builder lastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public Builder releaseDate(LocalDate releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public Builder roles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }

        public UserDto build() {
            return new UserDto(this);
        }
    }
}
