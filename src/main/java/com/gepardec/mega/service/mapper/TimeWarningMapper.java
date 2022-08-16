package com.gepardec.mega.service.mapper;

import com.gepardec.mega.application.configuration.ApplicationConfig;
import com.gepardec.mega.application.producer.ResourceBundleProducer;
import com.gepardec.mega.domain.model.monthlyreport.MappedTimeWarningTypes;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.rest.model.MappedTimeWarningDTO;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@ApplicationScoped
public class TimeWarningMapper {

    @Inject
    ResourceBundleProducer resourceBundleProducer;

    @Inject
    ApplicationConfig applicationConfig;

    @Inject
    TimeWarningPredicates timeWarningPredicates;





    public List<MappedTimeWarningDTO> map(List<TimeWarning> timeWarningList) {
        final HashMap<TimeWarning, ArrayList<MappedTimeWarningTypes>> timeWarningMapping = new HashMap<>();
        ResourceBundle templates = resourceBundleProducer.getWarningTemplateResourceBundle(applicationConfig.getDefaultLocale());

        timeWarningPredicates.getPredicateMap().forEach((mappedTimeWarningTypes, timeWarningPredicate) -> {
            timeWarningList.stream()
                    .filter(timeWarningPredicate)
                    .forEach(timeWarning -> appendMap(timeWarningMapping ,timeWarning, mappedTimeWarningTypes));
        });

        final List<MappedTimeWarningDTO> mappedTimeWarningList = new ArrayList<>();
        timeWarningMapping.forEach((timeWarning, types) -> {

            List<String> description = new ArrayList<>();

            types.forEach(type -> {
                String template = templates.getString("WARNING.TIME.".concat(type.name()));
                description.add(String.format(template, type.getTemplateValue(timeWarning)));
            });

            MappedTimeWarningDTO mtwDTO = MappedTimeWarningDTO.builder()
                    .date(timeWarning.getDate())
                    .description(String.join(" ", description))
                    .build();

            mappedTimeWarningList.add(mtwDTO);
        });

        return mappedTimeWarningList;
    }

    private void appendMap(Map<TimeWarning, ArrayList<MappedTimeWarningTypes>> map, TimeWarning tw, MappedTimeWarningTypes type) {
        if (map.containsKey(tw)) {
            map.get(tw).add(type);
        } else {
            map.put(tw, new ArrayList<>(List.of(type)));
        }
    }

}
