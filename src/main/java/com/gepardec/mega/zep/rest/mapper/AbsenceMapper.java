package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.entity.ZepAbsence;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;

@ApplicationScoped
public class AbsenceMapper implements Mapper<AbsenceTime, ZepAbsence> {

    @Inject
    Logger logger;

    @Override
    public AbsenceTime map(ZepAbsence zepAbsence) {

        if (zepAbsence == null){
            logger.info("ZEP REST implementation -- While trying to map ZepAbsence to AbsenceTime, ZepAbsence was null");
            return null;
        }

        try {

            String reason = zepAbsence.getAbsenceReason() == null ? "" : zepAbsence.getAbsenceReason().getName();

            AbsenceTime absenceTime = AbsenceTime.builder()
                    .id(zepAbsence.getId())
                    .userId(zepAbsence.getEmployeeId())
                    .fromDate(zepAbsence.getStartDate())
                    .toDate(zepAbsence.getEndDate())
                    .fromTime("" + zepAbsence.getFrom())
                    .toTime("" + zepAbsence.getTo())
                    .reason(reason)
                    .isHalfADay(false)                      //TODO: Check if needed, don't see any purpose and ZEP REST doesn't provide this value
                    .accepted(zepAbsence.isApproved())
                    .comment(zepAbsence.getNote())
                    .timezone(zepAbsence.getTimezone())
                    .suppressMails(true)                    //TODO: Check if needed, don't see any purpose and ZEP REST doesn't provide this value
                    .created(zepAbsence.getCreated())
                    .modified(zepAbsence.getModified())
                    .attributes(null)
                    .build();

            logger.debug("Mapped ZepAbsence to AbsenceTime -- some values have been hardcoded and not fetched from the ZEP rest client");
            return absenceTime;

        }catch (Exception e){
            throw new ZepServiceException("While trying to map ZepAbsence to AbsenceTime, an error occurred", e);
        }

    }
}
