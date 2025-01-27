package com.gepardec.mega.rest.provider;

import jakarta.enterprise.context.RequestScoped;

import java.time.YearMonth;

@RequestScoped
public class ManagementPayrollMonthProvider implements PayrollMonthProvider {

    private YearMonth payrollMonth;

    @Override
    public YearMonth getPayrollMonth() {
        if (payrollMonth == null) {
            payrollMonth = YearMonth.now().minusMonths(1);
        }
        return payrollMonth;
    }

    @Override
    public void overridePayrollMonth(YearMonth payrollMonth) {
        if (payrollMonth != null) {
            this.payrollMonth = payrollMonth;
        }
    }
}
