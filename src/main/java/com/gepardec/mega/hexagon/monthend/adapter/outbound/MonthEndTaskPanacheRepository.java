package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MonthEndTaskPanacheRepository implements PanacheRepository<MonthEndTaskEntity> {
}
