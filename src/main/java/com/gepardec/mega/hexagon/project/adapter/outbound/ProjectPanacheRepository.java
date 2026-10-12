package com.gepardec.mega.hexagon.project.adapter.outbound;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProjectPanacheRepository implements PanacheRepository<ProjectEntity> {
}
