package com.gepardec.mega.application.rest.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JacksonObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

    private final ObjectMapper MAPPER;

    public JacksonObjectMapperContextResolver() {
        MAPPER = new ObjectMapper();

//        MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
//        SimpleModule regularWorkingHoursModule = new SimpleModule("RegularWorkingHoursModule");
//        regularWorkingHoursModule.addSerializer((Class<Map<DayOfWeek, Duration>>) (Class) Map.class, new RegularWorkingHoursSerializer());
//        regularWorkingHoursModule.addDeserializer((Class<Map<DayOfWeek, Duration>>) (Class) Map.class, new RegularWorkingHoursDeserializer());
//        MAPPER.registerModule(regularWorkingHoursModule);

        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return MAPPER;
    }
}
