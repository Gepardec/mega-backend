package com.gepardec.mega.zep.mapper;

import com.gepardec.mega.domain.model.AbsenceTime;
import de.provantis.zep.FehlzeitType;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class AbsenceTimeMapper {

        public static List<AbsenceTime> mapList(List<FehlzeitType> absenceTimes) {
           if (absenceTimes == null) {
               return null;
           }

           return absenceTimes.stream()
                   .map(AbsenceTimeMapper::map)
                   .filter(Objects::nonNull)
                   .sorted(Comparator.comparing(AbsenceTime::fromDate))
                   .toList();
        }

        public static AbsenceTime map(FehlzeitType fehlzeitType) {
            if (fehlzeitType == null) {
                return null;
            }

            LocalDate startDate = MapperUtil.convertStringToDate(fehlzeitType.getStartdatum());
            LocalDate endDate = MapperUtil.convertStringToDate(fehlzeitType.getEnddatum());

            return AbsenceTime.builder()
                    .userId(fehlzeitType.getUserId())
                    .fromDate(startDate)
                    .toDate(endDate)
                    .reason(fehlzeitType.getFehlgrund())
                    .accepted(fehlzeitType.isGenehmigt())
                    .build();
        }

}
