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
public class ResourceFileServiceTests {
    @Inject
    ResourceFileService resourceFileService;

    @Test
    public void testEnv_thenReturnCorrectJsonRespPath() {
        String expectedPath = this.getClass().getResource("/zep/rest/testresponses").getPath();
        assertThat(resourceFileService.getFilesDir().getPath()).isEqualTo(expectedPath);
    }

    @Test
    public void testEnv_thenReturnCorrectFileContent() {
        assertThat(resourceFileService.getSingleFile("_test").get()).startsWith("ok");
    }

    @Test
    public void testEnv_thenReturnCorrectDirContents() {
        resourceFileService.getDirContents("_dirtest").forEach(
            content -> assertThat(content).startsWith("ok")
        );
    }


}
