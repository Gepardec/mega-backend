package com.gepardec.mega.domain.mapper.zep;

import de.provantis.zep.RegelarbeitszeitType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@QuarkusTest
public class RegularWorkingTimesMapperTest {

    @Inject
    RegularWorkingTimesMapper mapper;

    @Test
    void mapToDomain_whenNull_thenReturnsEmptyMap() {
        // When
        var result = mapper.mapToDomain(null);

        // Then
        assertThat(result).usingRecursiveComparison().isEqualTo(new HashMap<>());
    }

    @Test
    void mapToDomain_whenFullRegelarbeitszeitListeTypeTs_thenReturnsFullRegularWorkingTimes() {
        // Given
        Map<DayOfWeek, Duration> reference = initMap();
        reference.put(DayOfWeek.MONDAY, Duration.ofHours(8));
        reference.put(DayOfWeek.TUESDAY, Duration.ofHours(6));
        reference.put(DayOfWeek.WEDNESDAY, Duration.ofHours(4));
        reference.put(DayOfWeek.THURSDAY, Duration.ofHours(4));
        reference.put(DayOfWeek.FRIDAY, Duration.ofHours(5));
        var regelarbeitszeitListeTypeTs = createRegelarbeitszeitlisteFromMap(reference);

        // When
        Map<DayOfWeek, Duration> result = mapper.mapToDomain(regelarbeitszeitListeTypeTs);

        // Then
        assertThat(result).usingRecursiveComparison().isEqualTo(reference);
    }

    @Test
    void mapToDomain_whenWeekendDays_thenReturnsNullWorkingTimes() {
        // Given
        Map<DayOfWeek, Duration> testValues = initMap();
        testValues.put(DayOfWeek.SATURDAY, Duration.ofHours(8));
        testValues.put(DayOfWeek.SUNDAY, Duration.ofHours(6));
        var regelarbeitszeitListeTypeTs = createRegelarbeitszeitlisteFromMap(testValues);

        // When
        Map<DayOfWeek, Duration> result = mapper.mapToDomain(regelarbeitszeitListeTypeTs);

        // Then
        assertThat(result.get(DayOfWeek.SATURDAY)).usingRecursiveComparison().isEqualTo(Duration.ofHours(0));
        assertThat(result.get(DayOfWeek.SUNDAY)).usingRecursiveComparison().isEqualTo(Duration.ofHours(0));
    }
    @Test
    void mapToDomain_whenMinutes_thenReturnsWorkingTimesExact() {
        // Given
        Map<DayOfWeek, Duration> reference = initMap();
        Duration mondayDuration = Duration.ofMinutes(232);
        reference.put(DayOfWeek.MONDAY, mondayDuration);
        var regelarbeitszeitListeTypeTs = createRegelarbeitszeitlisteFromMap(reference);

        // When
        Map<DayOfWeek, Duration> result = mapper.mapToDomain(regelarbeitszeitListeTypeTs);

        // Then
        assertThat(result.get(DayOfWeek.MONDAY)).isEqualTo(mondayDuration);
    }

    private Map<DayOfWeek, Duration> initMap() {
        int empty = 0;

        return new HashMap<>(Map.ofEntries(
                Map.entry(DayOfWeek.MONDAY, Duration.ofHours(empty)),
                Map.entry(DayOfWeek.TUESDAY, Duration.ofHours(empty)),
                Map.entry(DayOfWeek.WEDNESDAY, Duration.ofHours(empty)),
                Map.entry(DayOfWeek.THURSDAY, Duration.ofHours(empty)),
                Map.entry(DayOfWeek.FRIDAY, Duration.ofHours(empty)),
                Map.entry(DayOfWeek.SATURDAY, Duration.ofHours(empty)),
                Map.entry(DayOfWeek.SUNDAY, Duration.ofHours(empty))
        ));
    }

    private de.provantis.zep.RegelarbeitszeitListeTypeTs createRegelarbeitszeitlisteFromMap(Map<DayOfWeek, Duration> map) {
        var regelarbeitszeitListeTypeTs = new de.provantis.zep.RegelarbeitszeitListeTypeTs();
        var regelarbeitszeit = new RegelarbeitszeitType();

        map
            .keySet()
            .forEach(dayOfWeek ->
                    addDayToRegelarbeitszeit(
                        dayOfWeek,
                        (double) map.get(dayOfWeek).toMinutes() / 60,
                            regelarbeitszeit
                            ));

        regelarbeitszeitListeTypeTs.setRegelarbeitszeit(List.of(regelarbeitszeit));
        return regelarbeitszeitListeTypeTs;
    }

    private void addDayToRegelarbeitszeit(DayOfWeek dayOfWeek,
                                     Double hours,
                                     de.provantis.zep.RegelarbeitszeitType regelarbeitszeit) {

        switch(dayOfWeek) {
            case MONDAY -> regelarbeitszeit.setMontag(hours);
            case TUESDAY -> regelarbeitszeit.setDienstag(hours);
            case WEDNESDAY -> regelarbeitszeit.setMittwoch(hours);
            case THURSDAY -> regelarbeitszeit.setDonnerstag(hours);
            case FRIDAY -> regelarbeitszeit.setFreitag(hours);
            case SATURDAY -> regelarbeitszeit.setSamstag(hours);
            case SUNDAY -> regelarbeitszeit.setSonntag(hours);
        }
    }
}
