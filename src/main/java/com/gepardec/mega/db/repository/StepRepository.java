package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.employee.StepEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class StepRepository implements PanacheRepository<StepEntity> {

    public List<StepEntity> findAllSteps() {
        return findAll().list();
    }
}
