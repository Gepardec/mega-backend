package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.employee.Step;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class StepRepository implements PanacheRepository<Step> {

    public List<Step> findAllSteps() {
        return findAll().list();
    }
}
