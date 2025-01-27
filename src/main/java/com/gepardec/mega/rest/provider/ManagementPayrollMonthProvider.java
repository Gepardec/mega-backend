package com.gepardec.mega.rest.provider;

import jakarta.enterprise.context.RequestScoped;

import java.time.YearMonth;

import static com.gepardec.mega.rest.provider.PayrollContext.PayrollContextType.MANAGEMENT;

@RequestScoped
@PayrollContext(MANAGEMENT)
public class ManagementPayrollMonthProvider implements PayrollMonthProvider {

    private YearMonth payrollMonth;

    @Override
    public YearMonth getPayrollMonth() {
        if (payrollMonth == null) {
            payrollMonth = YearMonth.now().minusMonths(1);
        }
        return payrollMonth;
    }
}
