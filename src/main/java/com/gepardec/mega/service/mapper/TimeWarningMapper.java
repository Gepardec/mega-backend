package com.gepardec.mega.service.mapper;

import com.gepardec.mega.application.configuration.ApplicationConfig;
import com.gepardec.mega.application.producer.ResourceBundleProducer;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.rest.model.MappedTimeWarning;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.AbstractMap;
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

    private final static HashMap<TimeWarning, String> templateMapping = new HashMap<TimeWarning, String>( Map.ofEntries(
            new AbstractMap.SimpleEntry<TimeWarning, String>(TimeWarning.of(0.0,0.0,0.0), "warning.time"),
            new AbstractMap.SimpleEntry<TimeWarning, String>(TimeWarning.of(0.0,0.0,1.0), "warning.time.excesswork"),
            new AbstractMap.SimpleEntry<TimeWarning, String>(TimeWarning.of(0.0,1.0,1.0), "warning.time.excesswork.missingbreak"),
            new AbstractMap.SimpleEntry<TimeWarning, String>(TimeWarning.of(1.0,1.0,1.0), "warning.time.missingrest.missingbreak.excesswork"),
            new AbstractMap.SimpleEntry<TimeWarning, String>(TimeWarning.of(1.0,1.0,0.0), "warning.time.missingrest.missingbreak"),
            new AbstractMap.SimpleEntry<TimeWarning, String>(TimeWarning.of(1.0,0.0,0.0), "warning.time.missingrest"),
            new AbstractMap.SimpleEntry<TimeWarning, String>(TimeWarning.of(0.0,1.0,0.0), "warning.time.missingbreak"),
            new AbstractMap.SimpleEntry<TimeWarning, String>(TimeWarning.of(1.0,0.0,1.0), "warning.time.missingrest.excesswork")
    ));


    public MappedTimeWarning map(TimeWarning timeWarning){
        ResourceBundle templates = resourceBundleProducer.getWarningTemplateResourceBundle(applicationConfig.getDefaultLocale());

        String test;
        if(templateMapping.containsKey(timeWarning)){
             test = templateMapping.get(timeWarning);
        }else{
             test = "warning.time";
        }


        return MappedTimeWarning.builder()
                .date(timeWarning.getDate())
                .description(templates.getString(test))
                .build();
    }
}
