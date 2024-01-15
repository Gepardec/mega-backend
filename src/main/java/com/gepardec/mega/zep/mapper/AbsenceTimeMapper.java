package com.gepardec.mega.zep.mapper;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.*;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Map;

import static com.gepardec.mega.domain.utils.DateUtils.parseDateTime;

@ApplicationScoped
public class AbsenceTimeMapper {

        public List<AbsenceTime> mapList(List<FehlzeitType> absenceTimes) {
           if (absenceTimes == null)
               return null;

           return absenceTimes.stream()
                   .map(this::map)
                   .filter(Objects::nonNull)
                   .sorted(Comparator.comparing(AbsenceTime::getFromTime))
                   .collect(Collectors.toList());
        }

        public AbsenceTime map(FehlzeitType fehlzeitType) {
            if (fehlzeitType == null) {
                return null;
            }

            LocalDateTime startDate = parseDateTime(fehlzeitType.getStartdatum(), fehlzeitType.getVonZeit());
            LocalDateTime endDate = parseDateTime(fehlzeitType.getEnddatum(), fehlzeitType.getBisZeit());
            Map<String, String> attributes = convertAttributesToMap(fehlzeitType.getAttributes());

            return AbsenceTime.builder()
                    .id(fehlzeitType.getId())
                    .userId(fehlzeitType.getUserId())
                    .fromTime(startDate)
                    .fromTime(endDate)
                    .reason(fehlzeitType.getFehlgrund())
                    .isHalfADay(fehlzeitType.isIstHalberTag())
                    .accepted(fehlzeitType.isGenehmigt())
                    .comment(fehlzeitType.getBemerkung())
                    .timezone(fehlzeitType.getTimezone())
                    .suppressMails(fehlzeitType.isMailversandUnterdruecken())
                    .created(fehlzeitType.getCreated())
                    .modified(fehlzeitType.getModified())
                    .attributes()
                    .build();
        }

        public Map<String, String> convertAttributesToMap(Attributes attributes) {
            return attributes.getAttribute().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(AttributeType::getName, AttributeType::getValue));
        }
}
