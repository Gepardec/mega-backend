package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.employee.CommentEntity;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class CommentRepository implements PanacheRepository<CommentEntity> {

    @Inject
    EntityManager em;

    public List<CommentEntity> findAllCommentsBetweenStartDateAndEndDateAndAllOpenCommentsBeforeStartDateForEmail(LocalDate startDate, LocalDate endDate, String email) {
        return find("#Comment.findAllCommentsBetweenStartDateAndEndDateAndAllOpenCommentsBeforeStartDateForEmail",
                Parameters
                        .with("start", startDate)
                        .and("end", endDate)
                        .and("state", EmployeeState.OPEN)
                        .and("email", email))
                .list();
    }

    @Transactional
    public int setStatusDone(Long id) {
        return update("UPDATE CommentEntity c SET c.employeeState = :state WHERE id = :id",
                Parameters
                        .with("id", id)
                        .and("state", EmployeeState.DONE));
    }

    @Transactional
    public CommentEntity save(final CommentEntity comment) {
        this.persist(comment);
        return comment;
    }

    @Transactional
    public CommentEntity update(final CommentEntity comment) {
        return em.merge(comment);
    }

    @Transactional
    public boolean deleteComment(Long id) {
        return deleteById(id);
    }
}
