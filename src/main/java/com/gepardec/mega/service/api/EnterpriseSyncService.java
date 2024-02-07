package com.gepardec.mega.service.api;

import java.time.LocalDate;

public interface EnterpriseSyncService {

    boolean generateEnterpriseEntries(LocalDate date);
}
