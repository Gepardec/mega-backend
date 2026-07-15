package com.gepardec.mega.hexagon.recognition.adapter.outbound;

import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionCategory;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntry;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntryId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RecognitionEntryMapperTest {

    private final RecognitionEntryMapper mapper = Mappers.getMapper(RecognitionEntryMapper.class);

    @Test
    void updateEntity_shouldStoreSubmitterIdForNonAnonymousEntry() {
        UserId submitterId = UserId.of(Instancio.create(UUID.class));
        RecognitionEntry entry = RecognitionEntry.create(
                RecognitionEntryId.of(Instancio.create(UUID.class)),
                "Danke für den Einsatz.",
                RecognitionCategory.APPRECIATION,
                Instant.parse("2026-07-06T15:30:00Z"),
                submitterId
        );
        RecognitionEntryEntity entity = Instancio.create(RecognitionEntryEntity.class);

        mapper.updateEntity(entry, entity);

        assertThat(entity.getSubmittedBy()).isEqualTo(submitterId.value());
    }

    @Test
    void updateEntity_shouldRemoveSubmitterIdForAnonymousEntry() {
        RecognitionEntry entry = RecognitionEntry.create(
                RecognitionEntryId.of(Instancio.create(UUID.class)),
                "Danke für den Einsatz.",
                RecognitionCategory.APPRECIATION,
                Instant.parse("2026-07-06T15:30:00Z")
        );
        RecognitionEntryEntity entity = Instancio.create(RecognitionEntryEntity.class);

        mapper.updateEntity(entry, entity);

        assertThat(entity.getSubmittedBy()).isNull();
    }
}
