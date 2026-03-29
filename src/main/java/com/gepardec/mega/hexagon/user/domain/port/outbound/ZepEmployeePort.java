package com.gepardec.mega.hexagon.user.domain.port.outbound;

import com.gepardec.mega.hexagon.user.domain.model.ZepProfile;

import java.util.List;

public interface ZepEmployeePort {

    List<ZepProfile> fetchAll();
}
