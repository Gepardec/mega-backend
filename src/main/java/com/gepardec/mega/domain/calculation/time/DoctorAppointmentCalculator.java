package com.gepardec.mega.domain.calculation.time;

import com.gepardec.mega.domain.calculation.AbstractTimeWarningCalculationStrategy;
import com.gepardec.mega.domain.calculation.WarningCalculationStrategy;
import com.gepardec.mega.domain.model.Employee;

import com.gepardec.mega.domain.model.monthlyreport.AbsenteeType;
import com.gepardec.mega.domain.model.monthlyreport.JourneyTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarningType;
import de.provantis.zep.FehlzeitType;
import net.bytebuddy.asm.Advice;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorAppointmentCalculator extends AbstractTimeWarningCalculationStrategy implements WarningCalculationStrategy<TimeWarning>{

    private static final LocalTime START_HOUR_MORNING = LocalTime.of(8, 30);
    private static final LocalTime END_HOUR_MORNING = LocalTime.of(12,0);
    private static final LocalTime START_HOUR_AFTERNOON = LocalTime.of(12, 30);
    private static final LocalTime END_HOUR_AFTERNOON = LocalTime.of(17, 0);

    private static final String DOCTOR_APPOINTMENT = "Arztbesuch";

    public List<TimeWarning> calculate(List<ProjectEntry> projectEntries) {
        final List<TimeWarning> warnings = new ArrayList<>();
        final List<ProjectTimeEntry> projectTimeEntries =  projectEntries.stream()
                .filter(projectEntry -> projectEntry.getClass() == ProjectTimeEntry.class)
                .map(ProjectTimeEntry.class::cast)
                .collect(Collectors.toList());


        LocalTime projectEntryFromTime, projectEntryToTime;

        for(ProjectTimeEntry projectEntry: projectTimeEntries){
            if(projectEntry.getProcess() == null || !projectEntry.getProcess().equals(DOCTOR_APPOINTMENT)) continue;

            projectEntryFromTime = projectEntry.getFromTime().toLocalTime();
            projectEntryToTime = projectEntry.getToTime().toLocalTime();

            if(projectEntryFromTime.isBefore(START_HOUR_MORNING)
            || (projectEntryToTime.isAfter(END_HOUR_MORNING)
                && projectEntryToTime.isBefore(START_HOUR_AFTERNOON))
            || (projectEntryFromTime.isAfter(END_HOUR_MORNING)
                && projectEntryFromTime.isBefore(START_HOUR_AFTERNOON))
            || projectEntryToTime.isAfter(END_HOUR_AFTERNOON)
            || (projectEntryFromTime.isBefore(END_HOUR_MORNING)
                && projectEntryToTime.isAfter(START_HOUR_AFTERNOON))){
                warnings.add(createTimeWarning(projectEntry));
            }
        }

        return warnings;
    }


    private TimeWarning createTimeWarning(ProjectTimeEntry absence){
        TimeWarning timeWarning = new TimeWarning();
        timeWarning.setDate(absence.getDate());
        timeWarning.getWarnings().add("Warnung: Doktorbuchung außerhalb der Sollarbeitszeit");
        timeWarning.getWarningTypes().add(TimeWarningType.WRONG_DOCTOR_APPOINTMENT);

        return timeWarning;
    }
}
