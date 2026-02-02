package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.project.ProjectEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
public class ProjectRepository implements PanacheRepository<ProjectEntity> {

    @Inject
    EntityManager em;

    public ProjectEntity findByName(String name) {
        return find("name", name).firstResult();
    }

    public ProjectEntity merge(ProjectEntity project) {
        return em.merge(project);
    }
}
