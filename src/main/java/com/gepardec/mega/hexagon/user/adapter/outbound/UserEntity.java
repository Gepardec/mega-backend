package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.user.domain.model.PersonioProfile;
import com.gepardec.mega.hexagon.user.domain.model.ZepProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Set;
import java.util.UUID;

@Entity(name = "HexagonUserEntity")
@Table(name = "hexagon_users")
public class UserEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "email")
    private String email;

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "status", nullable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "roles")
    private Set<String> roles;

    @Column(name = "zep_username")
    private String zepUsername;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "zep_profile")
    private ZepProfile zepProfile;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "personio_profile")
    private PersonioProfile personioProfile;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getZepUsername() {
        return zepUsername;
    }

    public void setZepUsername(String zepUsername) {
        this.zepUsername = zepUsername;
    }

    public ZepProfile getZepProfile() {
        return zepProfile;
    }

    public void setZepProfile(ZepProfile zepProfile) {
        this.zepProfile = zepProfile;
    }

    public PersonioProfile getPersonioProfile() {
        return personioProfile;
    }

    public void setPersonioProfile(PersonioProfile personioProfile) {
        this.personioProfile = personioProfile;
    }
}
