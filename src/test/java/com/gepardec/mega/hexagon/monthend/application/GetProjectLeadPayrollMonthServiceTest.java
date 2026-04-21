package com.gepardec.mega.hexagon.monthend.application;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class GetProjectLeadPayrollMonthServiceTest {

    private final GetProjectLeadPayrollMonthService service = new GetProjectLeadPayrollMonthService();

    @Test
    void getPayrollMonth_shouldAlwaysReturnPreviousMonth() {
        YearMonth payrollMonth = service.getPayrollMonth();

        assertThat(payrollMonth).isEqualTo(YearMonth.now().minusMonths(1));
    }
}
