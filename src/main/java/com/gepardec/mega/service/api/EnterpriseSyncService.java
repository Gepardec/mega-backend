package com.gepardec.mega.service.api;

import java.time.YearMonth;

public interface EnterpriseSyncService {

    boolean generateEnterpriseEntries(YearMonth payrollMonth);
}
