package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.zep.rest.entity.ZepAbsence;
import jakarta.enterprise.context.ApplicationScoped;

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
