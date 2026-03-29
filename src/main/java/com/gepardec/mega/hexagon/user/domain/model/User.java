package com.gepardec.mega.hexagon.user.domain.model;

import java.util.HashSet;
import java.util.Set;

public class User {

    private final UserId id;
    private Email email;
    private FullName name;
    private UserStatus status;
    private Set<Role> roles;
    private ZepProfile zepProfile;
    private PersonioProfile personioProfile;

    private User(UserId id, Email email, FullName name, UserStatus status, Set<Role> roles,
                 ZepProfile zepProfile, PersonioProfile personioProfile) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.status = status;
        this.roles = roles;
        this.zepProfile = zepProfile;
        this.personioProfile = personioProfile;
    }

    public static User create(UserId id, ZepProfile zepProfile, Set<Role> roles) {
        Email email = Email.of(zepProfile.email());
        FullName name = FullName.of(zepProfile.firstname(), zepProfile.lastname());
        return new User(id, email, name, UserStatus.ACTIVE, new HashSet<>(roles), zepProfile, null);
    }

    public static User reconstitute(UserId id, Email email, FullName name, UserStatus status,
                                    Set<Role> roles, ZepProfile zepProfile, PersonioProfile personioProfile) {
        return new User(id, email, name, status, new HashSet<>(roles), zepProfile, personioProfile);
    }

    public void syncFromZep(ZepProfile zepProfile) {
        this.zepProfile = zepProfile;
        this.email = Email.of(zepProfile.email());
        this.name = FullName.of(zepProfile.firstname(), zepProfile.lastname());
    }

    public void syncFromPersonio(PersonioProfile personioProfile) {
        if (personioProfile != null) {
            this.personioProfile = personioProfile;
        }
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = new HashSet<>(roles);
    }

    public UserId id() {
        return id;
    }

    public Email email() {
        return email;
    }

    public FullName name() {
        return name;
    }

    public UserStatus status() {
        return status;
    }

    public Set<Role> roles() {
        return Set.copyOf(roles);
    }

    public ZepProfile zepProfile() {
        return zepProfile;
    }

    public PersonioProfile personioProfile() {
        return personioProfile;
    }
}
