package com.gepardec.mega.rest.mapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.Streams;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.tuple.Pair;


import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegularWorkingHoursSerializer extends StdSerializer<Map<DayOfWeek, Duration>> {

    public RegularWorkingHoursSerializer() {
        this(null);
    }

    protected RegularWorkingHoursSerializer(Class<Map<DayOfWeek, Duration>> t) {
        super(t);
    }

    @Override
    public void serialize(Map<DayOfWeek, Duration> dayOfWeekDurationMap, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        List<Pair<String,String>> transformedMap = dayOfWeekDurationMap.entrySet().stream().map(dayOfWeekDurationEntry -> {
            // This will be a Json-Key and should not change based locale
            String newKey = dayOfWeekDurationEntry.getKey().getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH);
            String newVal = DurationFormatUtils.formatDuration(dayOfWeekDurationEntry.getValue().toMillis(), "HH:mm");
            return Pair.of(newKey, newVal);
        }).collect(Collectors.toList());

        jsonGenerator.writeStartObject();

        for (Pair<String,String> entry: transformedMap) {
            jsonGenerator.writeStringField(entry.getKey(), entry.getValue());
        }

        jsonGenerator.writeEndObject();
    }
}
