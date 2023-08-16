package com.gepardec.mega.personio.auth;

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
