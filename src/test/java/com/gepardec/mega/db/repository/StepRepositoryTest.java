package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.employee.StepEntity;
import com.gepardec.mega.domain.model.Role;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@Transactional
class StepRepositoryTest {

    @Inject
    StepRepository stepRepository;

    private StepEntity step;

    @BeforeEach
    void init() {
        step = initializeStepObject();
        stepRepository.persist(step);
    }

    @AfterEach
    void tearDown() {
        assertThat(stepRepository.deleteById(step.getId())).isTrue();
    }

    @Test
    void findAllSteps_whenCalled_thenReturnsCorrectAmountOfSteps() {
        List<StepEntity> result = stepRepository.findAllSteps();
        assertThat(result).hasSize(1);
    }

    @Test
    void findAllSteps_whenCalled_thenReturnedListContainsCorrectStep() {
        List<StepEntity> result = stepRepository.findAllSteps();
        assertThat(result).contains(step);
    }

    @Test
    void findAllSteps_whenCalledAndNoStepExists_thenReturnsEmptyListOfSteps() {
        stepRepository.deleteById(step.getId());
        List<StepEntity> result = stepRepository.findAllSteps();
        assertThat(result).isEmpty();
        createStep();
    }

    private void createStep() {
        step = new StepEntity();
        step.setName("TestName");
        step.setRole(Role.EMPLOYEE);
        step.setOrdinal(1);
        step.setStepEntries(null);
        stepRepository.persist(step);
    }

    private StepEntity initializeStepObject() {
        StepEntity initStep = new StepEntity();
        initStep.setName("TestName");
        initStep.setRole(Role.EMPLOYEE);
        initStep.setOrdinal(1);
        initStep.setStepEntries(null);

        return initStep;
    }
}
