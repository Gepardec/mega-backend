# Design

## Context

The two services build the same `WorkTimeReport` in three steps. Only the first step differs:

```
                     GetEmployeeWorkTimeService              GetProjectLeadWorkTimeService
1. pick scope        employees = {actor}                     projects  = findAllByLead(actor)
                     (findById; 404 if unknown,              employees = ZEP membership of those projects
                      400 if no ZEP username)                            -> findByZepUsernames (unknown skipped + WARN)
                     projects  = any (findByZepId per         projects filter = lead projects only
                                 booked project, unknown
                                 skipped + WARN)
2. fetch             1 ZEP attendance call                   1 ZEP attendance call per employee, merged (Mutiny)
3. assemble          group by project -> assembler           group by employee, project -> assembler
                     sort by project name                    sort by employee, then project name
```

Constraints:

- Both services run with `@Transactional(TxType.NEVER)` because ZEP calls fan out via Mutiny, and a propagated JTA context races with commit. This must stay.
- `employeeMonthTotalHours` must use all of an employee's attendances, before any project filter is applied (`worktime-report`).
- `Capacity`, the capacity gate and the `Capacity` contract schema come from `unify-monthend-reads-by-capacity`.

## Goals / Non-Goals

**Goals:**
- One use case, one service and one resource for the report. Only step 1 depends on capacity.
- Preserve each capacity's current content and error behavior exactly, except for ordering.

**Non-Goals:**
- Changing ZEP ports, adapters or rate behavior. Merging the two ZEP attendance adapters is a separate future idea.
- Making the warnings or absences endpoints capacity-aware.
- Batching the per-project `findByZepId` lookups of the employee path.

## Decisions

### D1 — Capacity selects a `WorkTimeScope`; steps 2 and 3 are shared

`GetWorkTimeReportUseCase.getWorkTime(UserId actorId, YearMonth month, Capacity capacity)`. The service resolves a `WorkTimeScope` and then runs one pipeline:

```
WorkTimeScope(Map<String, UserRef> employeesByZepUsername, ProjectResolver projects)

EMPLOYEE      employees = {actor}                       (same lookup and errors as today)
              projects  = zepId -> findByZepId(zepId)   (unknown -> empty, WARN)
CONTROLLING  projects  = findAllByLead -> map by zepId; empty -> empty report
              employees = membership(projects) -> findByZepUsernames; empty -> empty report
              resolver  = zepId -> map.get(zepId)       (non-lead projects -> empty, no WARN)

shared: fetch attendances for all employees in parallel (Mutiny merge, as in the lead path)
        map ZEP usernames to employees              (unknown -> skipped, WARN)
        resolve each distinct booked project once   (via the scope's resolver)
        WorkTimeReport.assemble(month, attendancesByEmployee, projectsByZepId)   (see D5)
```

The resolver is a small functional type local to the application layer. The two branches differ in whether an unresolvable project gets a WARN: a booked project the employee path cannot find is unexpected, while a non-lead project in the lead path is expected. That difference is kept explicit in each resolver.

`WorkTimeScope` and the resolver live in `worktime.application` as implementation details of the service. They are not domain concepts, because they only describe how the report is fetched.

*Alternative:* two strategy beans selected by capacity. Rejected as extra wiring for two branches that are a few lines each.

### D2 — No server-side sorting

The `sorted(...)` calls are removed. The client already groups and sorts entries (by project or by employee, with German collation), so a server order would only be redundant work. Tests compare entries order-insensitively.

### D3 — One `WorkTime` tag and one resource

The report and warnings operations share the `WorkTime` tag, so the generator emits one `WorkTimeApi`. `WorkTimeEmployeeResource` and `WorkTimeProjectLeadResource` merge into `WorkTimeResource`:

- `getWorkTimeReport(payrollMonth, capacity)`: `@MegaRolesAllowed(Role.EMPLOYEE)`, then the capacity gate from `unify-monthend-reads-by-capacity`.
- `getEmployeeWarnings(payrollMonth)`: `@MegaRolesAllowed(Role.EMPLOYEE)`, unchanged.

Role annotations move from the class to the methods because they now differ per method.

*Alternative:* keep `WorkTimeEmployee` for warnings and add a new tag for the report. Rejected: role-named tags are exactly what this redesign removes, and the frontend has to regenerate clients anyway.

The rule-settings operations keep their own tag, renamed from `WorkTimeAdmin` to `WorkTimeWarningRules`. Admin is a role, not a capacity, and rule settings are a separate resource with their own caller, so they get a resource-named tag of their own. `WorkTimeWarningRuleResource` keeps its class-level `@MegaRolesAllowed(Role.ADMIN)` and only implements the renamed `WorkTimeWarningRulesApi`.

*Alternative:* fold the rule-settings operations into `WorkTime` as well. Rejected: the merged resource would lose the class-level `ADMIN` guard, every admin method would need its own guard, and an admin-only write resource would be mixed with employee reads.

### D4 — Contract and validation

`GET /worktime/{payrollMonth}` (operationId `getWorkTimeReport`) reuses `PayrollMonthPathParameter` and adds a required `capacity` query parameter referencing the shared `Capacity` schema. The rejection behavior for a missing or unknown capacity is the same as for month-end (see that change's design, D3). `/worktime/warnings/{payrollMonth}` does not collide with the new path because it has one more segment. `GET /worktime/warning-rules` has the same shape as `/worktime/{payrollMonth}`, but the literal path takes precedence over the template, and `warning-rules` is not a valid payroll month anyway.

### D5 — Assembly lives on the report model

`WorkTimeReport.assemble(month, attendancesByEmployee, projectsByZepId)` builds the report from already resolved data:

```
per employee: total = sum of all attendances   (before any project filter)
              group by projectZepId -> resolved projects only -> WorkTimeEntry.of(..., total)
```

`WorkTimeEntry.of` sums billable and non-billable hours for one project. The previous `WorkTimeReportAssembler` domain service is removed. It was stateless and only summed values of the report's own records, which left the report model as a plain data holder. Keeping assembly on the model puts the rule that `employeeMonthTotalHours` counts bookings on unresolved projects in one place, where pure unit tests cover it.

The service resolves employees and projects before calling `assemble`, so the domain stays free of port calls and logging. Deciding whether an unresolved project gets a WARN stays in the scope's resolver (D1).

*Alternative:* keep the assembler domain service. Rejected: it needs no injection, I/O or second aggregate, which are the usual reasons for a domain service.

## Risks / Trade-offs

- [The employee path now goes through the parallel fetch code] → With one employee it degenerates to a single call. Covered by the existing employee service tests after the merge.
- [Clients or tests that relied on order] → The contract now says the order is unspecified. Backend tests compare order-insensitively, and the frontend already sorts.
- [The frontend breaks on deploy (hard cut)] → Ship with the frontend migration, as for `unify-monthend-reads-by-capacity`.

## Migration Plan

Apply after `unify-monthend-reads-by-capacity` is archived. Deploy together with the frontend release that switches paths and regenerates the worktime client. Rollback means reverting both. There is no data or schema change.
