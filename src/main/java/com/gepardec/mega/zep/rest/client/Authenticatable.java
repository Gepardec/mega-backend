package com.gepardec.mega.zep.rest.client;

import com.gepardec.mega.application.configuration.ZepConfig;

public interface Authenticatable {
    default String getAuthHeaderValue() {
        return "Bearer " + ZepConfig.getRestBearerToken();
    }

}
