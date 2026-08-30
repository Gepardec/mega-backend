package com.gepardec.mega.hexagon.recognition.adapter.outbound;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RecognitionEntryPanacheRepository implements PanacheRepository<RecognitionEntryEntity> {
}
