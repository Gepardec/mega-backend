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
        File[] files = filesSubDir.listFiles();
        if (files == null) {
            throw   new RuntimeException("No test files found in directory: " + filesSubDir.getPath());
        }

        return Arrays.stream(files)
                .sorted((f1, f2) -> f1.getName().compareTo(f2.getName()))
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
        try{
        String resourcesPath = this.getClass().getResource("/").getPath();
        String absPath = resourcesPath + path;
        return new File(absPath);
        }catch(Exception e){
            throw new RuntimeException("Error reading resource Path for test resources", e);
        }
    }
}
