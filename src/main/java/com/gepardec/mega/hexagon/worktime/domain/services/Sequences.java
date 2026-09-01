package com.gepardec.mega.hexagon.worktime.domain.services;

import com.gepardec.mega.hexagon.worktime.domain.model.SequenceEntry;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public final class Sequences {
    private Sequences() {
    }

    /**
     * Creates a list of {@link SequenceEntry} objects from the given list of values,
     * where each entry contains the value and its successor (if any).
     *
     * @param values the list of values to create sequence entries from
     * @param <T>    the type of the values
     * @return a list of {@link SequenceEntry} objects with successors
     * @throws NullPointerException if the input list is null
     */
    public static <T> List<SequenceEntry<T>> withSuccessor(List<T> values) {
        Objects.requireNonNull(values, "values must not be null");
        List<T> snapshot = List.copyOf(values);
        return IntStream.range(0, snapshot.size())
                .mapToObj(index -> new SequenceEntry<>(
                        snapshot.get(index),
                        index + 1 < snapshot.size()
                                ? Optional.of(snapshot.get(index + 1))
                                : Optional.empty())
                )
                .toList();
    }
}
