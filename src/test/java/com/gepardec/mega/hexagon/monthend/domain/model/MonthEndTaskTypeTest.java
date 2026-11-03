package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.Capacity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class MonthEndTaskTypeTest {

    @ParameterizedTest
    @CsvSource({
            "EMPLOYEE_TIME_CHECK, false",
            "LEISTUNGSNACHWEIS, true",
            "PROJECT_LEAD_REVIEW, true",
            "ABRECHNUNG, false"
    })
    void isProjectLeadBulkCompletable_shouldAllowOnlyLeadReviewAndLeistungsnachweis(MonthEndTaskType type, boolean expected) {
        assertThat(type.isProjectLeadBulkCompletable()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "EMPLOYEE_TIME_CHECK, EMPLOYEE",
            "LEISTUNGSNACHWEIS, CONTROLLING",
            "PROJECT_LEAD_REVIEW, CONTROLLING",
            "ABRECHNUNG, CONTROLLING"
    })
    void dutyOf_shouldMapEachTypeToItsCapacity(MonthEndTaskType type, Capacity expected) {
        assertThat(type.dutyOf()).isEqualTo(expected);
    }
}
