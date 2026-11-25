package com.gepardec.mega.hexagon.worktime.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TravelAllowanceTasksTest {

    private static final int PROJECT = 10;

    @Test
    void from_whenExactName_thenProjectDeclaresOwnTask() {
        TravelAllowanceTasks tasks = TravelAllowanceTasks.from(List.of(new ProjectTask(PROJECT, 500, "Reisepauschale")));

        assertThat(tasks.declaresOwnTask(PROJECT)).isTrue();
        assertThat(tasks.ownTaskIds(PROJECT)).containsExactly(500);
    }

    @Test
    void from_whenLowerCaseAndSurroundingWhitespace_thenProjectDeclaresOwnTask() {
        TravelAllowanceTasks tasks = TravelAllowanceTasks.from(List.of(new ProjectTask(PROJECT, 500, " reisepauschale ")));

        assertThat(tasks.declaresOwnTask(PROJECT)).isTrue();
    }

    @Test
    void from_whenLongerName_thenProjectDeclaresNoOwnTask() {
        TravelAllowanceTasks tasks = TravelAllowanceTasks.from(List.of(new ProjectTask(PROJECT, 500, "Reisepauschale Kunde")));

        assertThat(tasks.declaresOwnTask(PROJECT)).isFalse();
        assertThat(tasks.ownTaskIds(PROJECT)).isEmpty();
    }

    @Test
    void from_whenSeveralMatchingTasksOnOneProject_thenAllCount() {
        TravelAllowanceTasks tasks = TravelAllowanceTasks.from(List.of(
                new ProjectTask(PROJECT, 500, "Reisepauschale"),
                new ProjectTask(PROJECT, 501, "REISEPAUSCHALE")));

        assertThat(tasks.ownTaskIds(PROJECT)).containsExactlyInAnyOrder(500, 501);
    }

    @Test
    void from_whenInternReisepauschaleTask_thenItIsDropped() {
        TravelAllowanceTasks tasks = TravelAllowanceTasks.from(List.of(
                new ProjectTask(TravelAllowanceTasks.INTERN_PROJECT_ZEP_ID,
                        TravelAllowanceTasks.INTERN_REISEPAUSCHALE_TASK_ID, "Reisepauschale")));

        assertThat(tasks.declaresOwnTask(TravelAllowanceTasks.INTERN_PROJECT_ZEP_ID)).isFalse();
    }

    @Test
    void from_whenInternHasAnotherMatchingTask_thenInternStillDeclaresNone() {
        TravelAllowanceTasks tasks = TravelAllowanceTasks.from(List.of(
                new ProjectTask(TravelAllowanceTasks.INTERN_PROJECT_ZEP_ID,
                        TravelAllowanceTasks.INTERN_REISEPAUSCHALE_TASK_ID, "Reisepauschale"),
                new ProjectTask(TravelAllowanceTasks.INTERN_PROJECT_ZEP_ID, 874, "Reisepauschale")));

        assertThat(tasks.declaresOwnTask(TravelAllowanceTasks.INTERN_PROJECT_ZEP_ID)).isFalse();
        assertThat(tasks.ownTaskIds(TravelAllowanceTasks.INTERN_PROJECT_ZEP_ID)).isEmpty();
    }

    @Test
    void ownTaskIds_whenUnknownOrNullProject_thenEmpty() {
        TravelAllowanceTasks tasks = TravelAllowanceTasks.from(List.of(new ProjectTask(PROJECT, 500, "Reisepauschale")));

        assertThat(tasks.ownTaskIds(99)).isEmpty();
        assertThat(tasks.declaresOwnTask(99)).isFalse();
        assertThat(tasks.ownTaskIds(null)).isEmpty();
    }

    @Test
    void none_thenNoProjectDeclaresAnOwnTask() {
        assertThat(TravelAllowanceTasks.none().declaresOwnTask(PROJECT)).isFalse();
    }
}
