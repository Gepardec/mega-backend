package com.gepardec.mega.domain.mapper.zep;

import de.provantis.zep.RegelarbeitszeitListeTypeTs;
import de.provantis.zep.RegelarbeitszeitType;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@ApplicationScoped
public class RegularWorkingTimesMapper implements ZepMapper<Map<DayOfWeek, Duration>, de.provantis.zep.RegelarbeitszeitListeTypeTs> {

    @Override
    public Map<DayOfWeek, Duration> mapToDomain(RegelarbeitszeitListeTypeTs object) {
        if (object == null || object.getRegelarbeitszeit().isEmpty()) {
            return new HashMap<>();
        }

        List<de.provantis.zep.RegelarbeitszeitType> regelarbeitszeitList = object.getRegelarbeitszeit();
        de.provantis.zep.RegelarbeitszeitType regelarbeitszeitType = regelarbeitszeitList.get(regelarbeitszeitList.size() - 1);

        return mapSingleToDomain(regelarbeitszeitType);
    }

    private static Map<DayOfWeek, Duration> mapSingleToDomain(RegelarbeitszeitType regelarbeitszeitType) {
        final long emptyValue = 0L;


        return new HashMap<>(Map.ofEntries(
                Map.entry(DayOfWeek.MONDAY, getRegularWorkingTimeFromDoubleValue(regelarbeitszeitType.getMontag())),
                Map.entry(DayOfWeek.TUESDAY, getRegularWorkingTimeFromDoubleValue(regelarbeitszeitType.getDienstag())),
                Map.entry(DayOfWeek.WEDNESDAY, getRegularWorkingTimeFromDoubleValue(regelarbeitszeitType.getMittwoch())),
                Map.entry(DayOfWeek.THURSDAY, getRegularWorkingTimeFromDoubleValue(regelarbeitszeitType.getDonnerstag())),
                Map.entry(DayOfWeek.FRIDAY, getRegularWorkingTimeFromDoubleValue(regelarbeitszeitType.getFreitag())),
                Map.entry(DayOfWeek.SATURDAY, toDuration(emptyValue)),
                Map.entry(DayOfWeek.SUNDAY, toDuration(emptyValue))
        ));
    }

    private static Duration getRegularWorkingTimeFromDoubleValue(Double workingTime) {
        final int minutesInHour = 60;
        final double emptyValue = 0.0;

        if (workingTime == null) {
            workingTime = emptyValue;
        }

        return toDuration((long) (workingTime * minutesInHour));
    }

    private static Duration toDuration(long regelarbeitszeitStunden) {
        return Duration.ofMinutes(Math.max(0L, regelarbeitszeitStunden));
    }
}
