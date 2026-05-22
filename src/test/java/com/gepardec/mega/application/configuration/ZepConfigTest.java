package com.gepardec.mega.application.configuration;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

class ZepConfigTest {

    private static final String ORIGIN = "https://www.zep-online.de/zepgepardecservices_test";

    private ZepConfig zepConfig;

    @BeforeEach
    void setUp() throws URISyntaxException, MalformedURLException {
        zepConfig = new ZepConfig();
        zepConfig.origin = new URI(ORIGIN).toURL();
    }

    @Test
    void buildProjectUrl_shouldAssembleUrlForProjectId() {
        String projectUrl = zepConfig.buildProjectUrl(1234);

        assertThat(projectUrl).isEqualTo(
                ORIGIN + "/view/index.php?menu=ProjektVerwaltungMgr&modelContentMenu=true&contentModelId=1234"
        );
    }

    @Test
    void buildEmployeeUrl_shouldAssembleUrlForUsername() {
        String employeeUrl = zepConfig.buildEmployeeUrl(ZepUsername.of("john.doe"));

        assertThat(employeeUrl).isEqualTo(
                ORIGIN + "/view/index.php?menu=MitarbeiterVerwaltungMgr&modelContentMenu=true&mgr=MitarbeiterProjektzeitMgr&contentModelId=john.doe"
        );
    }
}
