package com.gepardec.mega.zep.mapper;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import de.provantis.zep.AttributeType;
import de.provantis.zep.AttributesType;
import de.provantis.zep.ProjektzeitType;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.gepardec.mega.domain.utils.DateUtils.parseDate;

@ApplicationScoped
public class ProjectTimeMapper {

        public static List<ProjectTime> mapList(List<ProjektzeitType> projectTimes) {
           if (projectTimes == null)
               return null;

           return projectTimes.stream()
                   .map(ProjectTimeMapper::map)
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());
        }

        public ProjectTime map(ZepEmployee zepEmployee) {
            if (projektzeitType == null)
                return null;
            

            return Employee.builder()
            .userId(zepEmployee.getPersonalNumber())
            .email(zepEmployee.getEmail())
            .title(zepEmployee.getTitle())
            .firstname(zepEmployee.getFirstname())
            .lastname(zepEmployee.getLastname())
            .salutation(zepEmployee.getSalutation())
            .releaseDate(zepEmployee.getReleaseDate())
            .workDescription(zepEmployee.)
            .language(zepEmployee.getLanguage())
                    .active( zepEmployee.)
                    .build();
            //private Map<DayOfWeek, Duration> regularWorkingHours;
                    .build();
        }

}
