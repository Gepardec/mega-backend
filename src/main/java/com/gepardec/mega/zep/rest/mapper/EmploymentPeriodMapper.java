package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.EmploymentPeriod;
import com.gepardec.mega.zep.rest.dto.ZepEmploymentPeriod;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class EmploymentPeriodMapper implements Mapper<EmploymentPeriod, ZepEmploymentPeriod> {

    @Override
    public EmploymentPeriod map(ZepEmploymentPeriod zepEmploymentPeriod) {
        return new EmploymentPeriod(
                zepEmploymentPeriod.startDate().toLocalDate(),
                Optional.ofNullable(zepEmploymentPeriod.endDate()).map(LocalDateTime::toLocalDate).orElse(null)
        );
    }
}
