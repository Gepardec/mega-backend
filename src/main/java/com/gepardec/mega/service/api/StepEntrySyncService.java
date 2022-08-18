package com.gepardec.mega.service.api;

import java.time.YearMonth;

public interface StepEntrySyncService {

    boolean generateStepEntriesFromEndpoint();

    boolean generateStepEntriesFromEndpoint(YearMonth date);

    boolean generateStepEntriesFromScheduler();
}
