package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.project.ProjectCommentEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@ApplicationScoped
@Transactional
public class ProjectCommentRepository implements PanacheRepository<ProjectCommentEntity> {

    @Inject
    EntityManager em;

    @Inject
    Logger logger;

    public List<ProjectCommentEntity> findByProjectNameAndDateBetween(String projectName, LocalDate from, LocalDate to) {
        return find("#ProjectComment.findByProjectNameAndEntryDateBetween",
                Parameters
                        .with("projectName", projectName)
                        .and("start", from)
                        .and("end", to))
                .list();
    }

    public List<ProjectCommentEntity> findByProjectNameWithDate(String projectName, LocalDate entryDate) {
        return findByProjectNameAndDateBetween(
                projectName,
                entryDate.with(TemporalAdjusters.firstDayOfMonth()),
                entryDate.with(TemporalAdjusters.lastDayOfMonth())
        );
    }

    public boolean update(ProjectCommentEntity comment) {
        try {
            em.merge(comment);
            return true;
        } catch (Exception exception) {
            logger.error("An exception occurred during updating enterprise entry", exception);
            return false;
        }
    }

    public ProjectCommentEntity save(ProjectCommentEntity projectComment) {
        this.persist(projectComment);
        return projectComment;
    }
}
