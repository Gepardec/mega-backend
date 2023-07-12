package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.project.Project;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
public class ProjectRepository implements PanacheRepository<Project> {

    @Inject
    EntityManager em;

    public Project findByName(String name) {
        return find("name", name).firstResult();
    }

    public Project merge(Project project) {
        return em.merge(project);
    }
}
