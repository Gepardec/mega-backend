package com.gepardec.mega.db.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class PrematureEmployeeCheckRepository {

    @Inject
    EntityManager em;
}
