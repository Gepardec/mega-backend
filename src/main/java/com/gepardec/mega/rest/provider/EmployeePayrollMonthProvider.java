package com.gepardec.mega.rest.provider;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.service.api.StepEntryService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.YearMonth;

@RequestScoped
public class EmployeePayrollMonthProvider implements PayrollMonthProvider {

    @Inject
    UserContext userContext;

    @Inject
    StepEntryService stepEntryService;

    private YearMonth payrollMonth;

    @Override
    public YearMonth getPayrollMonth() {
        if (payrollMonth == null) {
            payrollMonth = getInitialPayrollMonth();
        }
        return payrollMonth;
    }

    private YearMonth getInitialPayrollMonth() {
        LocalDate now = LocalDate.now();
        LocalDate midOfCurrentMonth = now.withDayOfMonth(14);
        LocalDate firstOfPreviousMonth = now.minusMonths(1).withDayOfMonth(1);

        if (now.isAfter(midOfCurrentMonth) && isMonthConfirmedFromEmployee(userContext.getUser().getEmail(), firstOfPreviousMonth)) {
            return YearMonth.now();
        }

        return YearMonth.from(firstOfPreviousMonth);
    }

    private boolean isMonthConfirmedFromEmployee(String employeeEmail, LocalDate date) {
        return stepEntryService.findControlTimesStepEntry(employeeEmail, date).stream()
                .allMatch(stepEnry -> stepEnry.getState().equals(EmployeeState.DONE));
    }

    @Override
    public void overridePayrollMonth(YearMonth payrollMonth) {
        if (payrollMonth != null) {
            this.payrollMonth = payrollMonth;
        }
    }
}
