package com.gepardec.mega.hexagon.project.adapter.outbound;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
@Transactional
public class ProjectRepositoryAdapter implements ProjectRepository {

    @Inject
    ProjectPanacheRepository panache;

    @Inject
    ProjectMapper mapper;

    @Override
    public Optional<Project> findByZepId(int zepId) {
        return panache.find("zepId", zepId)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public List<Project> findAll() {
        return panache.listAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Project> findAllByIds(Set<ProjectId> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }

        return panache.list("id in ?1", projectIds.stream()
                        .map(ProjectId::value)
                        .toList()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void saveAll(List<Project> projects) {
        for (Project project : projects) {
            ProjectEntity entity = panache.find("id", project.getId().value())
                    .firstResultOptional()
                    .orElseGet(ProjectEntity::new);
            boolean isNew = entity.getId() == null;
            mapper.updateEntity(project, entity);
            if (isNew) {
                panache.persist(entity);
            } else {
                panache.getEntityManager().merge(entity);
            }
        }
    }
}
