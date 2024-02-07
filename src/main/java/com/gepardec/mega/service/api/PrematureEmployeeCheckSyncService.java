package com.gepardec.mega.service.api;

import java.time.LocalDate;

public interface PrematureEmployeeCheckSyncService {

    boolean syncPrematureEmployeeChecksWithStepEntries(LocalDate date);
}
