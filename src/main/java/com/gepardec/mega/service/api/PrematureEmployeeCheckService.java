package com.gepardec.mega.service.api;

import com.gepardec.mega.domain.model.PrematureEmployeeCheck;

import java.util.List;

public interface PrematureEmployeeCheckService {
    boolean addPrematureEmployeeCheck(PrematureEmployeeCheck prematureEmployeeCheck);

    List<PrematureEmployeeCheck> getPrematureEmployeeCheckForEmail(String email);

    boolean hasUserPrematureEmployeeCheck(String email);
}
