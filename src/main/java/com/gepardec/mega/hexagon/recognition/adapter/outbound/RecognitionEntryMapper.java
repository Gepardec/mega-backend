package com.gepardec.mega.hexagon.recognition.adapter.outbound;

import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntry;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntryId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface RecognitionEntryMapper {

    @Mapping(target = "id", source = "id.value")
    void updateEntity(RecognitionEntry entry, @MappingTarget RecognitionEntryEntity entity);

    RecognitionEntry toDomain(RecognitionEntryEntity entity);

    default RecognitionEntryId toRecognitionEntryId(UUID id) {
        return id == null ? null : RecognitionEntryId.of(id);
    }

    default UUID fromUserId(UserId userId) {
        return userId == null ? null : userId.value();
    }

    default UserId toUserId(UUID userId) {
        return userId == null ? null : UserId.of(userId);
    }
}
