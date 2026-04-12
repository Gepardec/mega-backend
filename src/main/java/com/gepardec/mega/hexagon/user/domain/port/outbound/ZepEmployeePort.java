package com.gepardec.mega.hexagon.user.domain.port.outbound;

import com.gepardec.mega.hexagon.user.domain.model.ZepEmployeeSyncData;

import java.util.List;

public interface ZepEmployeePort {

    List<ZepEmployeeSyncData> fetchAll();
}
