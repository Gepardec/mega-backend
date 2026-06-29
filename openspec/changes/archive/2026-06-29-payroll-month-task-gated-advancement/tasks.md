## 1. Project lead use case

- [x] 1.1 Add `UserId leadId` parameter to `GetProjectLeadPayrollMonthUseCase.getPayrollMonth()`
- [x] 1.2 Rewrite `GetProjectLeadPayrollMonthService`: inject `MonthEndTaskRepository`; return current month if `findLeadProjectTasks(leadId, currentMonth)` is non-empty, else previous month
- [x] 1.3 Update `MonthEndResource.getProjectLeadPayrollMonth()` to pass `authenticatedActorContext.userId()` to the use case

## 2. Employee use case

- [x] 2.1 Extend `GetEmployeePayrollMonthService.getPayrollMonth()`: add OR condition — if `findOpenSubjectTasks(actorId, currentMonth)` is non-empty, return current month (before the existing previous-month check)

## 3. Tests

- [x] 3.1 Rewrite `GetProjectLeadPayrollMonthServiceTest` with scenarios: current-month tasks exist → current month; no current-month tasks → previous month
- [x] 3.2 Add test cases to `GetEmployeePayrollMonthServiceTest`: previous month has open tasks + current month has open tasks → current month; previous month has open tasks + current month empty → previous month
- [x] 3.3 Update `MonthEndResourceTest` for the project lead payroll month endpoint (now passes actor ID)
