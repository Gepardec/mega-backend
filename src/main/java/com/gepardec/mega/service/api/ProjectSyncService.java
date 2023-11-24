package com.gepardec.mega.service.api;

import java.time.LocalDate;

public interface ProjectSyncService {

    boolean generateProjects();

    boolean generateProjects(LocalDate date);
}
