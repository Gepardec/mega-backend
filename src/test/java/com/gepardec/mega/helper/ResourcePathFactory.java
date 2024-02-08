package com.gepardec.mega.helper;

import jakarta.enterprise.inject.Produces;

public class ResourcePathFactory {
    @Produces @ResourcePath
    public String resourcePath = "/zep/rest/testresponses";
}
