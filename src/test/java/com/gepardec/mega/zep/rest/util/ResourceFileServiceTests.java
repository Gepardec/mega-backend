package com.gepardec.mega.zep.rest.util;

import com.gepardec.mega.helper.ResourceFileService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@QuarkusTest
class ResourceFileServiceTests {
    @Inject
    ResourceFileService resourceFileService;

    @Test
    void testEnv_thenReturnCorrectJsonRespPath() {
        String expectedPath = this.getClass().getResource("/zep/rest/testresponses").getPath();
        assertThat(resourceFileService.getFilesDir().getPath()).isEqualTo(expectedPath);
    }

    @Test
    void testEnv_thenReturnCorrectFileContent() {
        assertThat(resourceFileService.getSingleFile("_test").get()).startsWith("ok");
    }

    @Test
    void testEnv_thenReturnCorrectDirContents() {
        resourceFileService.getDirContents("_dirtest").forEach(
            content -> assertThat(content).startsWith("ok")
        );
    }


}
