## 1. OpenAPI Contract

- [x] 1.1 Add `GET /monthend/payroll-month/employee` path to `src/main/resources/openapi/paths/monthend.yaml` — response is a `string` in `yyyy-MM` format, requires `EMPLOYEE` role
- [x] 1.2 Add `GET /monthend/payroll-month/project-lead` path to `src/main/resources/openapi/paths/monthend.yaml` — response is a `string` in `yyyy-MM` format, requires `PROJECT_LEAD` role
- [x] 1.3 Verify generated Java API interface `MonthEndApi` includes the two new methods after build (`mvn generate-sources` or `mvn quarkus:dev`)

## 2. Application Inbound Ports

- [x] 2.1 Create `GetEmployeePayrollMonthUseCase` interface in `monthend/application/port/inbound/` — method returns `YearMonth`, takes `UserId actorId`
- [x] 2.2 Create `GetProjectLeadPayrollMonthUseCase` interface in `monthend/application/port/inbound/` — method returns `YearMonth`, no parameters needed

## 3. Application Services

- [x] 3.1 Create `GetEmployeePayrollMonthService` in `monthend/application/` — if `findOpenEmployeeTasks(actorId, prevMonth)` is empty return current month, else return previous month
- [x] 3.2 Create `GetProjectLeadPayrollMonthService` in `monthend/application/` — return `YearMonth.now().minusMonths(1)`

## 4. REST Adapter

- [x] 4.1 Add `GetEmployeePayrollMonthUseCase` and `GetProjectLeadPayrollMonthUseCase` to `MonthEndResource` constructor injection
- [x] 4.2 Implement the `getEmployeePayrollMonth()` method in `MonthEndResource` — delegate to use case, annotate `@MegaRolesAllowed(Role.EMPLOYEE)`, return the resolved `YearMonth` as a string
- [x] 4.3 Implement the `getProjectLeadPayrollMonth()` method in `MonthEndResource` — delegate to use case, annotate `@MegaRolesAllowed(Role.PROJECT_LEAD)`, return the resolved `YearMonth` as a string

## 5. Tests

- [x] 5.1 Unit test `GetEmployeePayrollMonthService`: open tasks in prev month → returns prev month
- [x] 5.2 Unit test `GetEmployeePayrollMonthService`: no open tasks in prev month → returns current month
- [x] 5.3 Unit test `GetEmployeePayrollMonthService`: no tasks at all for prev month → returns current month
- [x] 5.4 Unit test `GetProjectLeadPayrollMonthService`: always returns previous month
- [x] 5.5 REST integration test: `GET /monthend/payroll-month/employee` — authenticated employee with open tasks returns prev month string
- [x] 5.6 REST integration test: `GET /monthend/payroll-month/employee` — authenticated employee with no open tasks returns current month string
- [x] 5.7 REST integration test: `GET /monthend/payroll-month/project-lead` — authenticated project lead returns prev month string
- [x] 5.8 REST integration test: `GET /monthend/payroll-month/project-lead` — non-project-lead actor receives 403
