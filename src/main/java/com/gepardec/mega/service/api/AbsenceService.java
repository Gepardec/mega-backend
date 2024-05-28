package com.gepardec.mega.service.api;

import com.gepardec.mega.domain.model.AbsenceTime;

import java.time.LocalDate;
import java.util.List;

public interface AbsenceService {
    int numberOfFridaysAbsent(List<AbsenceTime> absences);
    int getNumberOfDaysAbsent(List<AbsenceTime> absences, LocalDate date);
}
