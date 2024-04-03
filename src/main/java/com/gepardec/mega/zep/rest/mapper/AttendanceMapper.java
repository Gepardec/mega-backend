package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.BillabilityPreset;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.mapper.MapperUtil;
import com.gepardec.mega.zep.rest.entity.ZepAttendance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class AttendanceMapper implements Mapper<ProjectTime, ZepAttendance> {

    @Inject
    Logger logger;

    @Override
    public ProjectTime map(ZepAttendance zepAttendance) {
        if (zepAttendance == null) {
            logger.info("ZEP REST implementation -- While trying to map ZepAttendance to ProjectTime, ZepAttendance was null");
            return null;
        }

        try {

            Boolean isBillable = zepAttendance.billable().id() <= BillabilityPreset.BILLABLE_FIXED.getZepId();
            Boolean locationPrRelevant = null;
            if (zepAttendance.workLocationIsProjectRelevant() != null) {
                locationPrRelevant = zepAttendance.workLocationIsProjectRelevant();
            }

            String duration = null;
            if (zepAttendance.duration() != null ) {
                BigDecimal bigDecimal = BigDecimal.valueOf(zepAttendance.duration());
                int hours = bigDecimal.intValue();
                int minutes = bigDecimal
                        .subtract(BigDecimal.valueOf(hours))
                        .multiply(BigDecimal.valueOf(60)).intValue();
                duration = "" + LocalTime.of(hours, minutes);
            }

            return ProjectTime.builder()
                    .userId(zepAttendance.employeeId())
                    .duration(duration)
                    .isBillable(isBillable)
                    .build();

        }catch (Exception e){
            throw new ZepServiceException("Error while mapping ZepAttendance to ProjectTime", e);
        }
    }
}
