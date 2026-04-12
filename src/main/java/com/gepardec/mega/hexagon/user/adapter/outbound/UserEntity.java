package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.user.domain.model.Role;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.HashSet;
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

    @ElementCollection
    @CollectionTable(name = "hexagon_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    @Column(name = "zep_username")
    private String zepUsername;

    @Column(name = "personio_id")
    private Integer personioId;

    @ElementCollection
    @CollectionTable(name = "hexagon_user_employment_periods", joinColumns = @JoinColumn(name = "user_id"))
    private Set<UserEmploymentPeriodEmbeddable> employmentPeriods = new HashSet<>();

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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getZepUsername() {
        return zepUsername;
    }

    public void setZepUsername(String zepUsername) {
        this.zepUsername = zepUsername;
    }

    public Integer getPersonioId() {
        return personioId;
    }

    public void setPersonioId(Integer personioId) {
        this.personioId = personioId;
    }

    public Set<UserEmploymentPeriodEmbeddable> getEmploymentPeriods() {
        return employmentPeriods;
    }

    public void setEmploymentPeriods(Set<UserEmploymentPeriodEmbeddable> employmentPeriods) {
        this.employmentPeriods = employmentPeriods;
    }
}
