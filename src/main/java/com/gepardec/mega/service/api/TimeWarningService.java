package com.gepardec.mega.service.api;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.WorkTimeBookingWarning;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;

import java.util.List;

public interface TimeWarningService {
    List<WorkTimeBookingWarning> getAllTimeWarningsForEmployeeAndMonth(List<AbsenceTime> absences, List<ProjectEntry> projectEntries, Employee employee);
}
