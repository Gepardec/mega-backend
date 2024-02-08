package com.gepardec.mega.helper;

import jakarta.enterprise.context.Dependent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Dependent
public class ResourceFileService {
    private File filesDir;

    public File getFilesDir() {
        return filesDir;
    }

    public ResourceFileService(@ResourcePath String filesPath) {
        File filesDir = getFileOfResourcesPath(filesPath);
        if (filesDir.isDirectory()) {
            this.filesDir = filesDir;
        }
    }

    public Optional<String> getSingleFile(String path) {
        File file = new File(filesDir.getPath() + "/" + path);
        System.out.println(file.getPath());
        if (!file.isFile()) {
            return Optional.empty();
        }
        return Optional.of(readFile(file));
    }

    public List<String> getDirContents(String path) {
        File filesSubDir = new File(filesDir.getPath() + "/" + path);
        return Arrays.stream(filesSubDir.listFiles())
                .map(ResourceFileService::readFile)
                .toList();
    }


    private static String readFile(File f) {
        try {
            return FileUtils.readFileToString(f, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getFileOfResourcesPath(String path) {
        String resourcesPath = this.getClass().getResource("/").getPath();
        String absPath = resourcesPath + path;
        return new File(absPath);
    }
}
