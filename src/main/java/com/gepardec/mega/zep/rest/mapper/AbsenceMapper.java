package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.dto.ZepAbsence;
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

            String reason = zepAbsence.absenceReason() == null ? "" : zepAbsence.absenceReason().name();

            AbsenceTime absenceTime = AbsenceTime.builder()
                    .userId(zepAbsence.employeeId())
                    .fromDate(zepAbsence.startDate())
                    .toDate(zepAbsence.endDate())
                    .reason(reason)
                    .accepted(zepAbsence.approved())
                    .build();

            logger.debug("Mapped ZepAbsence to AbsenceTime -- some values have been hardcoded and not fetched from the ZEP rest client");
            return absenceTime;

        }catch (Exception e){
            throw new ZepServiceException("While trying to map ZepAbsence to AbsenceTime, an error occurred", e);
        }

    }
}
