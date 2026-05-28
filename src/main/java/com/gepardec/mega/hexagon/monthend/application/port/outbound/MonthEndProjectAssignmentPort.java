package com.gepardec.mega.hexagon.monthend.application.port.outbound;

import java.time.YearMonth;
import java.util.Set;

public interface MonthEndProjectAssignmentPort {

    Set<String> findAssignedUsernames(int zepProjectId, YearMonth month);
}
