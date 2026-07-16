package com.gepardec.mega.hexagon.worktime.domain.services;

import com.gepardec.mega.hexagon.worktime.domain.model.SequenceEntry;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class SequencesTest {

    @Test
    void withSuccessor_shouldReturnEachValueWithItsSuccessorIncludingFinalValue() {
        String first = Instancio.create(String.class);
        String second = Instancio.create(String.class);
        String last = Instancio.create(String.class);

        List<SequenceEntry<String>> result = Sequences.withSuccessor(List.of(first, second, last));

        assertThat(result).containsExactly(
                new SequenceEntry<>(first, Optional.of(second)),
                new SequenceEntry<>(second, Optional.of(last)),
                new SequenceEntry<>(last, Optional.empty()));
    }

    @Test
    void withSuccessor_shouldSupportEmptyAndSingletonSequences() {
        String only = Instancio.create(String.class);

        assertThat(Sequences.withSuccessor(List.of())).isEmpty();
        assertThat(Sequences.withSuccessor(List.of(only)))
                .containsExactly(new SequenceEntry<>(only, Optional.empty()));
    }

    @Test
    void withSuccessor_shouldRejectNullCollectionsAndElements() {
        assertThatNullPointerException().isThrownBy(() -> Sequences.withSuccessor(null));
        assertThatNullPointerException().isThrownBy(() -> Sequences.withSuccessor(Arrays.asList("value", null)));
    }
}
