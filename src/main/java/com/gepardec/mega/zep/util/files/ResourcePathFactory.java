package com.gepardec.mega.zep.util.files;

import jakarta.enterprise.inject.Produces;

public class ResourcePathFactory {
    @Produces @ResourcePath
    public String resourcePath = "";
}
