package com.gepardec.mega.hexagon.project.adapter.outbound;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class ProjectRepositoryAdapter implements ProjectRepository {

    @Inject
    ProjectPanacheRepository panache;

    @Override
    public Optional<Project> findByZepId(int zepId) {
        return panache.find("zepId", zepId)
                .firstResultOptional()
                .map(this::toDomain);
    }

    @Override
    public List<Project> findAll() {
        return panache.listAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void saveAll(List<Project> projects) {
        for (Project project : projects) {
            ProjectEntity entity = panache.find("id", project.id().value())
                    .firstResultOptional()
                    .orElseGet(ProjectEntity::new);
            toEntity(project, entity);
            if (entity.getId() == null) {
                panache.persist(entity);
            } else {
                panache.getEntityManager().merge(entity);
            }
        }
    }

    private Project toDomain(ProjectEntity entity) {
        return Project.reconstitute(
                ProjectId.of(entity.getId()),
                entity.getZepId(),
                entity.getName(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getLeads() != null ? entity.getLeads() : java.util.Set.of()
        );
    }

    private void toEntity(Project project, ProjectEntity entity) {
        entity.setId(project.id().value());
        entity.setZepId(project.zepId());
        entity.setName(project.name());
        entity.setStartDate(project.startDate());
        entity.setEndDate(project.endDate());
        entity.setLeads(new java.util.HashSet<>(project.leads()));
    }
}
