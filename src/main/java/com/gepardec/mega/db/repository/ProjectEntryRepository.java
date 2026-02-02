package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.project.ProjectEntryEntity;
import com.gepardec.mega.db.entity.project.ProjectStep;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ProjectEntryRepository implements PanacheRepository<ProjectEntryEntity> {

    @Inject
    EntityManager em;

    @Inject
    UserTransaction tx;

    @Inject
    Logger logger;

    public List<ProjectEntryEntity> findByNameAndDate(String projectName, LocalDate from, LocalDate to) {
        return find("#ProjectEntry.findAllProjectEntriesForProjectNameInRange",
                Parameters
                        .with("projectName", projectName)
                        .and("start", from)
                        .and("end", to))
                .list();
    }

    public boolean updateProjectEntry(ProjectEntryEntity projectEntity) {
        try {
            tx.begin();
            em.merge(projectEntity);
            tx.commit();
            return true;
        } catch (Exception exception) {
            logger.error(exception.getMessage(), exception);
            return false;
        }
    }

    public ProjectEntryEntity findByNameAndEntryDateAndStep(String projectName, LocalDate entryDate, ProjectStep projectStep) {
        return find("#ProjectEntry.findProjectEntryByNameAndEntryDateAndStep",
                Parameters
                        .with("projectName", projectName)
                        .and("entryDate", entryDate)
                        .and("projectStep", projectStep))
                .firstResult();
    }
}
