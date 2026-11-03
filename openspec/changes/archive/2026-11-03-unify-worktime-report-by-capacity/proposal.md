## Why

The work time report is the last read that is still split by role: `GET /worktime/employee/{payrollMonth}` and `GET /worktime/projects/{payrollMonth}` return the same `WorkTimeReport` and differ only in which (employee × project) rows they contain. Each has its own resource, use case and service, and the two services reimplement the same fetch-and-assemble pipeline around two ways of picking the employees and projects. `unify-monthend-reads-by-capacity` introduced the actor capacity for exactly this kind of read. This change applies it to work time so that clients read every capacity-dependent view the same way.

## What Changes

- **BREAKING:** `GET /worktime/employee/{payrollMonth}` and `GET /worktime/projects/{payrollMonth}` are replaced by `GET /worktime/{payrollMonth}?capacity=EMPLOYEE|CONTROLLING`. `capacity` is required, follows the `shared-actor-capacity` rules (`400` without it, `403` without the capacity's role), and the response shape is unchanged.
- The content per capacity is unchanged. `EMPLOYEE` returns the caller's own bookings on all their projects. `CONTROLLING` returns every employee's bookings on the projects the caller leads. `employeeMonthTotalHours` still covers all of an employee's projects in both capacities.
- **BREAKING (contract):** the order of report entries is no longer defined. The server stops sorting, because the client already groups and sorts entries for display.
- **BREAKING (generated client types only):** the report and warnings operations move to one `WorkTime` contract tag, replacing `WorkTimeEmployee` and `WorkTimeProjectLead`. The warnings path, its behavior and its operation ID do not change. The rule-settings operations' tag is renamed from `WorkTimeAdmin` to `WorkTimeWarningRules`, so tags name resources rather than roles.
- No deprecation period. The old endpoints are removed in this change.

## Capabilities

### New Capabilities

_None._ Capacity rules come from `shared-actor-capacity`, introduced by `unify-monthend-reads-by-capacity`.

### Modified Capabilities

- `worktime-rest-api`: the role-specific report endpoints are replaced by one capacity-parameterized endpoint. The actor-derivation and role-access requirements now refer to it.
- `worktime-report`: the per-employee and per-lead queries become the `EMPLOYEE` and `CONTROLLING` capacity scopes of one report query, and entry order is defined as unspecified.
- `worktime-report-assembler`: all requirements are removed. Hour aggregation and entry construction become part of the report model, and their behavior is already specified in `worktime-report`.
- `monthend-rest-api`: the requirement that keeps work time out of the overview points to the new work time endpoint.

## Impact

- **API contract:** two operations are removed and one is added (`getWorkTimeReport`). The tags are merged into `WorkTime`, so the generated `WorkTimeEmployeeApi` and `WorkTimeProjectLeadApi` become `WorkTimeApi`, and `WorkTimeAdminApi` becomes `WorkTimeWarningRulesApi`. The frontend must switch paths, send `capacity`, and regenerate clients. Frontend work is out of scope for this change.
- **Application:** `GetEmployeeWorkTimeUseCase` and `GetProjectLeadWorkTimeUseCase` become one `GetWorkTimeReportUseCase`. Only the step that picks employees and projects depends on capacity. Fetching attendances and assembling entries is shared.
- **REST adapter:** the two worktime resources merge into one, which also hosts warnings. `WorkTimeWarningRuleResource` stays separate with its class-level `ADMIN` guard and only implements the renamed interface.
- **Domain:** `WorkTimeReportAssembler` is removed. `WorkTimeReport.assemble` and `WorkTimeEntry.of` take over hour aggregation and entry construction. The report's fields are unchanged.
- **Unchanged:** ZEP ports and adapters, the snapshot ports, the warnings and absences use cases, and the transaction setup (`TxType.NEVER`).
- **Depends on:** `unify-monthend-reads-by-capacity` (for `Capacity`, the capacity gate and the shared `Capacity` contract schema). Apply after it.
