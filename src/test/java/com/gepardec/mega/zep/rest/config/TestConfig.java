package com.gepardec.mega.zep.rest.config;

import com.gepardec.mega.zep.util.files.ResourcePath;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class TestConfig {
    @Produces @ResourcePath
    String resourcePath = "/zep/rest/testresponses";
}
