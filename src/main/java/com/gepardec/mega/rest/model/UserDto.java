package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.domain.model.Role;

import java.time.LocalDate;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

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

    public UserDto() {
    }

    public UserDto(long dbId, String userId, String email, String firstname, String lastname, LocalDate releaseDate, Set<Role> roles) {
        this.dbId = dbId;
        this.userId = userId;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.releaseDate = releaseDate;
        this.roles = roles;
    }

    public static UserDtoBuilder builder() {
        return UserDtoBuilder.anUserDto();
    }

    public long getDbId() {
        return dbId;
    }

    @JsonProperty
    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public String getUserId() {
        return userId;
    }

    @JsonProperty
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    @JsonProperty
    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    @JsonProperty
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    @JsonProperty
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    @JsonProperty
    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    @JsonProperty
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public static final class UserDtoBuilder {
        private long dbId;
        private String userId;
        private String email;
        private String firstname;
        private String lastname;
        private LocalDate releaseDate;
        private Set<Role> roles;

        private UserDtoBuilder() {
        }

        public static UserDtoBuilder anUserDto() {
            return new UserDtoBuilder();
        }

        public UserDtoBuilder dbId(long dbId) {
            this.dbId = dbId;
            return this;
        }

        public UserDtoBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public UserDtoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserDtoBuilder firstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public UserDtoBuilder lastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public UserDtoBuilder releaseDate(LocalDate releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public UserDtoBuilder roles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }

        public UserDto build() {
            UserDto userDto = new UserDto();
            userDto.setDbId(dbId);
            userDto.setUserId(userId);
            userDto.setEmail(email);
            userDto.setFirstname(firstname);
            userDto.setLastname(lastname);
            userDto.setReleaseDate(releaseDate);
            userDto.setRoles(roles);
            return userDto;
        }
    }
}
