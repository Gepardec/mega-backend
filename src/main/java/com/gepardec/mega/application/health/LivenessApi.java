package com.gepardec.mega.application.health;

import jakarta.ws.rs.core.Response;

public interface LivenessApi {

    Response liveness();
}
