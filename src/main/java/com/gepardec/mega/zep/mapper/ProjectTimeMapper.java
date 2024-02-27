package com.gepardec.mega.zep.mapper;

import com.gepardec.mega.domain.model.ProjectTime;
import de.provantis.zep.AttributeType;
import de.provantis.zep.AttributesType;
import de.provantis.zep.ProjektzeitType;
import jakarta.enterprise.context.ApplicationScoped;

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
           if (projectTimes == null) {
               return null;
           }
           return projectTimes.stream()
                   .map(ProjectTimeMapper::map)
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());
        }

        public static ProjectTime map(ProjektzeitType projektzeitType) {
            if (projektzeitType == null) {
                return null;
            }
            LocalDate date = MapperUtil.convertStringToDate(projektzeitType.getDatum());

            Map<String, String> attributes = MapperUtil.convertAttributesToMap(projektzeitType.getAttributes());


            return ProjectTime.builder()
                    .id(projektzeitType.getId())
                    .userId(projektzeitType.getUserId())
                    .date(date)
                    .startTime(projektzeitType.getVon())
                    .endTime(projektzeitType.getBis())
                    .duration(projektzeitType.getDauer())
                    .isBillable(projektzeitType.isIstFakturierbar())
                    .isLocationRelevantToProject(projektzeitType.isIstOrtProjektRelevant())
                    .location(projektzeitType.getOrt())
                    .comment(projektzeitType.getBemerkung())
                    .projectNr(projektzeitType.getProjektNr())
                    .processNr(projektzeitType.getVorgangNr())
                    .task(projektzeitType.getTaetigkeit())
                    .startLocation(projektzeitType.getStart())
                    .endLocation(projektzeitType.getZiel())
                    .km(projektzeitType.getKm())
                    .amountPassengers(projektzeitType.getAnzahlMitfahrer())
                    .vehicle(projektzeitType.getFahrzeug())
                    .ticketNr(projektzeitType.getTicketNr())
                    .subtaskNr(projektzeitType.getTeilaufgabeNr())
                    .travelDirection(projektzeitType.getReiseRichtung())
                    .isPrivateVehicle(projektzeitType.isIstPrivatFahrzeug())
                    .created(projektzeitType.getCreated())
                    .modified(projektzeitType.getModified())
                    .attributes(attributes)
                    .build();
        }

}
