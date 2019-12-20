package com.gepardec.mega.security;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

@Data
@NoArgsConstructor
@SessionScoped
public class SessionUser implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String email;
    private String name;
    private String authToken;
    private String idToken;
    private String authorizationCode;
    private Role role;

    public void invalidate() {
        id = null;
        email = null;
        name = null;
        authToken = null;
        idToken = null;
        authorizationCode = null;
        role = null;
    }

    public boolean isLoggedIn() {
        return id != null;
    }
}
