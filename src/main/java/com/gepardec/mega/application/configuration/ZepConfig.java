package com.gepardec.mega.application.configuration;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URL;
import java.util.Objects;

@ApplicationScoped
public class ZepConfig {

    private static final String PROJECT_URL_PATH =
            "/view/index.php?menu=ProjektVerwaltungMgr&modelContentMenu=true&contentModelId=%d";
    private static final String EMPLOYEE_URL_PATH =
            "/view/index.php?menu=MitarbeiterVerwaltungMgr&modelContentMenu=true&mgr=MitarbeiterProjektzeitMgr&contentModelId=%s";

    @Inject
    @ConfigProperty(name = "mega.zep.admin-token")
    String authorizationToken;

    @Inject
    @ConfigProperty(name = "mega.zep.origin")
    URL origin;

    @Inject
    @ConfigProperty(name = "mega.zep.soap-path")
    String soapPath;

    @Inject
    @ConfigProperty(name = "mega.zep.rest-token")
    String restToken;


    public String getRestBearerToken() {
        return restToken;
    }

    public String getUrlAsString() {
        return "%s%s".formatted(origin.toString(), soapPath);
    }

    public String getUrlForFrontend() {
        return origin.toString();
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public String buildProjectUrl(int zepId) {
        return "%s%s".formatted(origin.toString(), PROJECT_URL_PATH.formatted(zepId));
    }

    public String buildEmployeeUrl(ZepUsername username) {
        Objects.requireNonNull(username, "username must not be null");
        return "%s%s".formatted(origin.toString(), EMPLOYEE_URL_PATH.formatted(username.value()));
    }

}
