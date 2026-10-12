package com.gepardec.mega.hexagon.worktime.application.port.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.domain.model.Absence;

import java.time.YearMonth;
import java.util.List;

public interface WorkTimeAbsenceZepPort {

    List<Absence> fetchAbsencesForEmployee(ZepUsername zepUsername, YearMonth month);
}
