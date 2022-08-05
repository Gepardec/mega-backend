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
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApplicationScoped
public class TimeWarningMapper {

    @Inject
    ResourceBundleProducer resourceBundleProducer;
    @Inject
    ApplicationConfig applicationConfig;

    private final Predicate<TimeWarning> missingRestTimePredicate = (w) -> {
        if(w.getMissingRestTime() != null){
            return w.getMissingRestTime().compareTo(0.0) > 0;
        }
        return false;
    };
    private final Predicate<TimeWarning> missingBreakTimePredicate = (w) -> {
        if(w.getMissingBreakTime() != null){
            return w.getMissingBreakTime().compareTo(0.0) > 0;
        }
        return false;
    };
    private final Predicate<TimeWarning> excessWorkPredicate = (w) -> {
        if(w.getExcessWorkTime() != null){
            return w.getExcessWorkTime().compareTo(0.0) > 0;
        }
        return false;
    };

    private final HashMap<TimeWarning, ArrayList<MappedTimeWarningTypes>> timeWarningMapping= new HashMap();

    public List<MappedTimeWarningDTO> map(List<TimeWarning> timeWarningList){
        ResourceBundle templates = resourceBundleProducer.getWarningTemplateResourceBundle(applicationConfig.getDefaultLocale());

        timeWarningList.stream()
                .filter(missingBreakTimePredicate)
                .forEach(timeWarning -> appendMap(timeWarning, MappedTimeWarningTypes.MISSINGBREAK));

        timeWarningList.stream()
                .filter(missingRestTimePredicate)
                .forEach(timeWarning -> appendMap(timeWarning, MappedTimeWarningTypes.MISSINGREST));

        timeWarningList.stream()
                .filter(excessWorkPredicate)
                .forEach(timeWarning -> appendMap(timeWarning, MappedTimeWarningTypes.EXCESSWORK));

        final List<MappedTimeWarningDTO> mappedTimeWarningList = new ArrayList<>();
        timeWarningMapping.forEach((timeWarning, types) -> {

            List<String> description = new ArrayList<>();

            types.forEach(type -> {
                String template = templates.getString("WARNING.TIME.".concat(type.name()));
                switch (type){
                    case MISSINGREST: description.add(String.format(template, timeWarning.getMissingRestTime()));break;
                    case EXCESSWORK: description.add(String.format(template, timeWarning.getExcessWorkTime()));break;
                    case MISSINGBREAK: description.add(String.format(template, timeWarning.getMissingBreakTime()));break;
                }

            });


            MappedTimeWarningDTO mtwDTO =  MappedTimeWarningDTO.builder()
                    .date(timeWarning.getDate())
                    .description(String.join(" ", description))
                    .build();


            mappedTimeWarningList.add(mtwDTO);
        });

        return mappedTimeWarningList;
    }

    private void appendMap(TimeWarning tw, MappedTimeWarningTypes type){
        if (timeWarningMapping.containsKey(tw)) {
            timeWarningMapping.get(tw).add(type);
        }else{
            timeWarningMapping.put(tw, new ArrayList<>(List.of(type)));
        }
    }

}
