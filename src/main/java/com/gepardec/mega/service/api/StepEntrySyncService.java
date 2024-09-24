package com.gepardec.mega.service.api;

import java.time.LocalDate;

public interface StepEntrySyncService {

    boolean generateStepEntries(LocalDate date);
}
