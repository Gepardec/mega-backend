package com.gepardec.mega.service.api;

import java.time.YearMonth;

public interface PrematureEmployeeCheckSyncService {

    boolean syncPrematureEmployeeChecksWithStepEntries(YearMonth yearMonth);
}
