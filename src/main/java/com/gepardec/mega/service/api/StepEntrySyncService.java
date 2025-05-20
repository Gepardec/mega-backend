package com.gepardec.mega.service.api;

import java.time.YearMonth;

public interface StepEntrySyncService {

    boolean generateStepEntries(YearMonth payrollMonth);
}
