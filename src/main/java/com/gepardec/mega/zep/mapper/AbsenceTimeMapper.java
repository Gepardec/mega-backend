package com.gepardec.mega.zep.mapper;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.*;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Map;

import static com.gepardec.mega.domain.utils.DateUtils.parseDate;
import static com.gepardec.mega.domain.utils.DateUtils.parseDateTime;

import de.provantis.zep.FehlzeitType;
import de.provantis.zep.AttributesType;
import de.provantis.zep.AttributeType;

@ApplicationScoped
public class AbsenceTimeMapper {

        public static List<AbsenceTime> mapList(List<FehlzeitType> absenceTimes) {
           if (absenceTimes == null)
               return null;

           return absenceTimes.stream()
                   .map(AbsenceTimeMapper::map)
                   .filter(Objects::nonNull)
                   .sorted(Comparator.comparing(AbsenceTime::getFromTime))
                   .collect(Collectors.toList());
        }

        public static AbsenceTime map(FehlzeitType fehlzeitType) {
            if (fehlzeitType == null) {
                return null;
            }

            LocalDate startDate = parseDate(fehlzeitType.getStartdatum());
            LocalDate endDate = parseDate(fehlzeitType.getEnddatum());
            Map<String, String> attributes = convertAttributesToMap(fehlzeitType.getAttributes());

            return AbsenceTime.builder()
                    .id(fehlzeitType.getId())
                    .userId(fehlzeitType.getUserId())
                    .fromDate(startDate)
                    .toDate(endDate)
                    .fromTime(fehlzeitType.getVonZeit())
                    .toTime(fehlzeitType.getBisZeit())
                    .reason(fehlzeitType.getFehlgrund())
                    .isHalfADay(fehlzeitType.isIstHalberTag())
                    .accepted(fehlzeitType.isGenehmigt())
                    .comment(fehlzeitType.getBemerkung())
                    .timezone(fehlzeitType.getTimezone())
                    .suppressMails(fehlzeitType.isMailversandUnterdruecken())
                    .created(fehlzeitType.getCreated())
                    .modified(fehlzeitType.getModified())
                    .attributes(attributes)
                    .build();
        }

        public static Map<String, String> convertAttributesToMap(AttributesType attributes) {
            return attributes.getAttribute().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(AttributeType::getName, AttributeType::getValue));
        }
}
