package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class ActiveMapper implements Mapper<Boolean, List<ZepEmploymentPeriod>> {
    @Override
    public Boolean map(List<ZepEmploymentPeriod> zepEmploymentPeriods) {
        if (zepEmploymentPeriods == null || zepEmploymentPeriods.isEmpty()) {
            return false;
        }


        return zepEmploymentPeriods.stream()
                .anyMatch(zepEmploymentPeriod -> zepEmploymentPeriod.endDate() == null);
    }
}
