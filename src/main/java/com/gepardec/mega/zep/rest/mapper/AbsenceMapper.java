package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.zep.mapper.MapperUtil;
import com.gepardec.mega.zep.mapper.ProjectTimeMapper;
import com.gepardec.mega.zep.rest.entity.*;
import com.gepardec.mega.zep.rest.service.EmployeeService;
import com.gepardec.mega.zep.rest.service.EmploymentPeriodService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AbsenceMapper implements Mapper<AbsenceTime, ZepAbsence> {

    @Override
    public AbsenceTime map(ZepAbsence zepAbsence) {
        if (zepAbsence == null)
            return null;

        return AbsenceTime.builder()
                .id(zepAbsence.getId())
                .userId(zepAbsence.getEmployeeId())
                .fromDate(zepAbsence.getStartDate())
                .toDate(zepAbsence.getEndDate())
                .fromTime("" + zepAbsence.getFrom())
                .toTime("" + zepAbsence.getTo())
                .reason(zepAbsence.getAbsenceReason())
                .isHalfADay(false)                      //TODO: Possible to set real val?
                .accepted(zepAbsence.isApproved())
                .comment(zepAbsence.getNote())
                .timezone(zepAbsence.getTimezone())
                .suppressMails(true)                    //TODO: Possible to set real val?
                .created(zepAbsence.getCreated())
                .modified(zepAbsence.getModified())
                .attributes(null)
                .build();
    }
}
