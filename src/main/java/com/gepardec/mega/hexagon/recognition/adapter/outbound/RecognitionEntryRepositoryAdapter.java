package com.gepardec.mega.hexagon.recognition.adapter.outbound;

import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntry;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntryStatus;
import com.gepardec.mega.hexagon.recognition.domain.port.outbound.RecognitionEntryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class RecognitionEntryRepositoryAdapter implements RecognitionEntryRepository {

    @Inject
    RecognitionEntryPanacheRepository panache;

    @Inject
    RecognitionEntryMapper mapper;

    @Override
    public void save(RecognitionEntry entry) {
        RecognitionEntryEntity entity = panache.find("id", entry.id().value())
                .firstResultOptional()
                .orElseGet(RecognitionEntryEntity::new);
        boolean isNew = entity.getId() == null;
        mapper.updateEntity(entry, entity);
        if (isNew) {
            panache.persist(entity);
        } else {
            panache.getEntityManager().merge(entity);
        }
    }

    @Override
    public List<RecognitionEntry> findByStatus(RecognitionEntryStatus status) {
        return panache.list("status", status).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
