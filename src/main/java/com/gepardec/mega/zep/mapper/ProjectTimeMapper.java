package com.gepardec.mega.zep.mapper;

import com.gepardec.mega.domain.model.ProjectTime;
import de.provantis.zep.ProjektzeitType;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProjectTimeMapper {

    public static List<ProjectTime> mapList(List<ProjektzeitType> projectTimes) {
        if (projectTimes == null) {
            return null;
        }
        return projectTimes.stream()
                .map(ProjectTimeMapper::map)
                .filter(Objects::nonNull)
                .toList();
    }

    public static ProjectTime map(ProjektzeitType projektzeitType) {
        if (projektzeitType == null) {
            return null;
        }

        return ProjectTime.builder()
                .userId(projektzeitType.getUserId())
                .duration(projektzeitType.getDauer())
                .isBillable(projektzeitType.isIstFakturierbar())
                .build();
    }
}
