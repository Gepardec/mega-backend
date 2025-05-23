package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.repository.StepRepository;
import com.gepardec.mega.domain.model.Step;
import com.gepardec.mega.service.api.StepService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class StepServiceImpl implements StepService {

    @Inject
    StepRepository stepRepository;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Step> getSteps() {
        return stepRepository.findAll().list()
                .stream()
                .map(s -> Step.builder()
                        .dbId(s.getId())
                        .name(s.getName())
                        .ordinal(s.getOrdinal())
                        .role(s.getRole())
                        .build())
                .toList();
    }
}
