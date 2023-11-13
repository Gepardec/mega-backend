package com.gepardec.mega.application.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RegularWorkingHoursSerializer extends JsonSerializer<Map<DayOfWeek, Duration>> {

    @Override
    public void serialize(Map<DayOfWeek, Duration> dayOfWeekDurationMap, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (dayOfWeekDurationMap == null) {
            return;
        }

        List<Pair<String, String>> transformedMap = dayOfWeekDurationMap.entrySet().stream().map(dayOfWeekDurationEntry -> {
            String newKey = dayOfWeekDurationEntry.getKey().name();
            String newVal = DurationFormatUtils.formatDuration(dayOfWeekDurationEntry.getValue().toMillis(), "HH:mm");
            return Pair.of(newKey, newVal);
        }).collect(Collectors.toList());

        jsonGenerator.writeStartObject();

        for (Pair<String, String> entry : transformedMap) {
            jsonGenerator.writeStringField(entry.getKey(), entry.getValue());
        }

        jsonGenerator.writeEndObject();
    }
}
