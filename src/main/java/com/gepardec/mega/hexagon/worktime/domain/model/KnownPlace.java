package com.gepardec.mega.hexagon.worktime.domain.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public enum KnownPlace {
    WIEN(Set.of("wien")),
    LINZ(Set.of("linz")),
    ST_POELTEN(Set.of("poelten", "stpoelten"));

    private static final Pattern NON_LETTERS = Pattern.compile("[^a-z]+");

    private final Set<String> words;

    KnownPlace(Set<String> words) {
        this.words = words;
    }

    /**
     * Recognises a known place in free text. Only whole words match, and the text must name exactly one known place.
     */
    public static Optional<KnownPlace> recognise(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        Set<String> textWords = new HashSet<>(Arrays.asList(NON_LETTERS.split(normalise(text))));
        var matches = Arrays.stream(values())
                .filter(place -> place.words.stream().anyMatch(textWords::contains))
                .toList();
        return matches.size() == 1 ? Optional.of(matches.getFirst()) : Optional.empty();
    }

    private static String normalise(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss");
    }
}
