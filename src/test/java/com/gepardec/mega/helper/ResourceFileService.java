package com.gepardec.mega.helper;

import jakarta.enterprise.context.Dependent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Dependent
public class ResourceFileService {
    private File filesDir;

    public File getFilesDir() {
        return filesDir;
    }

    public ResourceFileService(@ResourcePath String filesPath) {
        File filesDirectory = getFileOfResourcesPath(filesPath);
        if (filesDirectory.isDirectory()) {
            this.filesDir = filesDirectory;
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
        if (path == null) {
            return List.of();
        }
        File filesSubDir = new File(filesDir.getPath() + "/" + path);
        File[] files = filesSubDir.listFiles();
        if (files == null) {
            throw new RuntimeException("No test files found in directory: " + filesSubDir.getPath());
        }

        return Arrays.stream(files)
                .sorted(Comparator.comparing(File::getName))
//                .peek(f -> System.out.println(f.getName()))
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
        try {
            URL resource = getClass().getResource("/");
            if (resource == null) {
                throw new IOException("Resource not found: " + path);
            }
            String resourcesPath = resource.getPath();
            String absPath = resourcesPath + path;
            return new File(absPath);
        } catch (Exception e) {
            throw new RuntimeException("Error reading resource Path for test resources", e);
        }
    }
}
