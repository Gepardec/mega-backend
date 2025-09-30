package com.gepardec.mega.zep.rest.util;

import com.gepardec.mega.helper.ResourceFileService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class ResourceFileServiceTests {
    @Inject
    ResourceFileService resourceFileService;

    @Test
    void testEnv_thenReturnCorrectJsonRespPath() {
        URL resource = getClass().getResource("/zep/rest/testresponses");
        assertNotNull(resource);
        String expectedPath = resource.getPath();
        assertThat(resourceFileService.getFilesDir().getPath()).isEqualTo(expectedPath);
    }

    @Test
    void testEnv_thenReturnCorrectFileContent() {
        Optional<String> singleFile = resourceFileService.getSingleFile("_test");
        assertThat(singleFile).isPresent();
        assertThat(singleFile.get()).startsWith("ok");
    }

    @Test
    void testEnv_thenReturnCorrectDirContents() {
        resourceFileService.getDirContents("_dirtest").forEach(
                content -> assertThat(content).startsWith("ok")
        );
    }


}
