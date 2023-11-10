package com.gepardec.mega.application.jackson.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class RegularWorkingHoursDeserializer extends JsonDeserializer<Map<DayOfWeek, Duration>> {

    private final DateTimeFormatter billableTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public Map<DayOfWeek, Duration> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {


        Map<DayOfWeek, Duration> regularWorkingHours = new HashMap<DayOfWeek, Duration>();

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        if (node.asText().isEmpty()) {
            return null;
        }

        String mondayValue = node.get(DayOfWeek.MONDAY.name()).asText();
        String tuesdayValue = node.get(DayOfWeek.TUESDAY.name()).asText();
        String wednesdayValue = node.get(DayOfWeek.WEDNESDAY.name()).asText();
        String thursdayValue = node.get(DayOfWeek.THURSDAY.name()).asText();
        String fridayValue = node.get(DayOfWeek.FRIDAY.name()).asText();

        regularWorkingHours.put(DayOfWeek.MONDAY, parsedStringToDuration(mondayValue));
        regularWorkingHours.put(DayOfWeek.TUESDAY, parsedStringToDuration(tuesdayValue));
        regularWorkingHours.put(DayOfWeek.WEDNESDAY, parsedStringToDuration(wednesdayValue));
        regularWorkingHours.put(DayOfWeek.THURSDAY, parsedStringToDuration(thursdayValue));
        regularWorkingHours.put(DayOfWeek.FRIDAY, parsedStringToDuration(fridayValue));

        return regularWorkingHours;
    }


    private Duration parsedStringToDuration(String billableTimeFormat) {
        LocalTime parsed = LocalTime.parse(billableTimeFormat, billableTimeFormatter);
        return Duration.ofHours(parsed.getHour()).plusMinutes(parsed.getMinute());
    }
}
