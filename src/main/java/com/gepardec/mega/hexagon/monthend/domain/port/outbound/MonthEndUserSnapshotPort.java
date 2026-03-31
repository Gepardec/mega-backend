package com.gepardec.mega.hexagon.monthend.domain.port.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;

import java.util.List;

public interface MonthEndUserSnapshotPort {

    List<MonthEndUserSnapshot> findAll();
}
