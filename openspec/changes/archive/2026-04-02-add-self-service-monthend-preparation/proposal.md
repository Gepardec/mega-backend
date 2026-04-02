## Why

Employees can need to finish month-end work for a specific project before the scheduled end-of-month task generation runs, for example before a planned absence. In the new per-project month-end model, the real need is to prepare the actual month-end obligations early and optionally add a clarification in the same project context, not to reintroduce a separate premature-check aggregate.

## What Changes

- Add an explicit employee self-service flow to prepare month-end obligations for one project and month-end context at a time.
- Materialize the same employee-owned `MonthEndTask` obligations that scheduled generation would create for that project context, using the same business keys and idempotent behavior.
- Allow the employee to optionally create a `MonthEndClarification` in the same month/project/employee context as part of the preparation workflow, so explanatory text lives in the clarification model instead of on the task.
- Keep project-lead-owned tasks (`PROJECT_LEAD_REVIEW`, `ABRECHNUNG`) on the scheduled generation path; self-service preparation only creates employee-owned tasks.
- Prevent duplicates when self-service preparation and scheduled generation target the same month-end obligation.
- Keep the first implementation at the use-case boundary and supporting identity-resolution logic without introducing a REST controller yet.

## Capabilities

### New Capabilities
- `monthend-self-service-preparation`: Explicit per-project employee workflow to prepare employee-owned month-end tasks early and optionally add a clarification in the same context.

### Modified Capabilities
- `monthend-task-generation`: Generation must treat manually prepared employee-owned tasks as existing obligations and skip duplicates for the same business key.

## Impact

- Affected code:
  `com.gepardec.mega.hexagon.monthend` application services, persistence adapter, identity-resolution support, and tests
- Affected specs:
  new self-service preparation capability plus task-generation delta
- Affected APIs:
  no external REST API in the first implementation slice; work stops at the use-case boundary
- Persistence:
  stronger duplicate prevention for `MonthEndTask` business obligations, likely including database-level uniqueness
- Cross-capability interaction:
  optional creation of `MonthEndClarification` during the preparation workflow
