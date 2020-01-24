package com.gepardec.mega.aplication.security;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Objects;

@SessionScoped
public class SessionUser implements Serializable {
    private String id;
    private String email;
    private String idToken;
    // TODO: User can have more than one role
    private Role role;
    private boolean logged;

    public SessionUser() {
    }

    public void init(final String email, final String idToken, final int recht) {
        this.email = Objects.requireNonNull(email, "SessionUser must have an email");
        this.idToken = Objects.requireNonNull(idToken, "SessionUser must have an idToken");
        this.role = Role.forId(recht).orElse(null);
        this.logged = true;
    }

    public void checkForSameUser(String eMail) {
        if (Role.USER.equals(this.getRole()) && !this.getEmail().equals(eMail)) {
            throw new SecurityException("User with userrole can not update other users");
        }
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getIdToken() {
        return idToken;
    }

    public Role getRole() {
        return role;
    }

    public boolean isLogged() {
        return logged;
    }
}
