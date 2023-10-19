package com.gepardec.mega.personio.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {

    private boolean success;

    private AuthResponseData authResponseData;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public AuthResponseData getData() {
        return authResponseData;
    }

    public void setData(AuthResponseData authResponseData) {
        this.authResponseData = authResponseData;
    }
}
