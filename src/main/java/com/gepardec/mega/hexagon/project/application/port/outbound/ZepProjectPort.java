package com.gepardec.mega.hexagon.project.application.port.outbound;

import com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile;

import java.util.List;

public interface ZepProjectPort {

    List<ZepProjectProfile> fetchAll();

    List<String> fetchLeadUsernames(int zepId);
}
