package com.gepardec.mega.hexagon.monthend.domain.port.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;

import java.util.List;

public interface MonthEndProjectSnapshotPort {

    List<MonthEndProjectSnapshot> findAll();
}
