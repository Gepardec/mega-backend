package com.gepardec.mega.service.api;

import java.time.YearMonth;

public interface ProjectSyncService {

    boolean generateProjects(YearMonth payrollMonth);
}
