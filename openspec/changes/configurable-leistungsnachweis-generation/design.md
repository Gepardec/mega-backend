## Context

`LEISTUNGSNACHWEIS` (proof-of-performance) month-end tasks are generated in `MonthEndTaskPlanningService.planProjectTasks(...)`. Today the sole gate is `project.billable() && !activeLeadIds.isEmpty()`. Project leads cannot suppress the task for projects where the workflow does not apply.

The `Project` aggregate lives in the `project` bounded context and is largely ZEP-synced (`name`, dates, `billable`), with `leads` being the one MEGA-managed field. Month-end generation reads projects through the anti-corruption boundary `MonthEndProjectSnapshot`, produced by `ProjectSnapshotAdapter`/`MonthEndProjectSnapshotMapper`. The `project` BC currently exposes **no inbound adapter** — projects only leave the BC as internal snapshots for month-end.

There is no existing frontend path to read a lead's projects, so the toggle needs both a read and a write endpoint to be usable.

## Goals / Non-Goals

**Goals:**
- Let a project lead opt a specific project out of (and back into) `LEISTUNGSNACHWEIS` generation.
- Preserve current behaviour for every existing project (default enabled).
- Keep the flag owned by the `project` BC and carried to month-end via the existing snapshot boundary.
- Provide the read + write REST surface the frontend needs.

**Non-Goals:**
- Making other task types (`ABRECHNUNG`, `PROJECT_LEAD_REVIEW`, `EMPLOYEE_TIME_CHECK`) configurable.
- Retroactively adding or removing already-generated tasks when the flag changes.
- Any generic "task-type configuration" framework.
- Sourcing the flag from ZEP.

## Decisions

### The flag lives on the `Project` aggregate, defaulting to `true`
`leistungsnachweisEnabled` is a MEGA-managed boolean on `Project`, alongside `leads`. It defaults to `true` so opt-out semantics preserve today's behaviour. `Project.create(...)` sets it `true`; `withSyncedZepData(...)` and `withLeads(...)` preserve it; a new `withLeistungsnachweisEnabled(boolean)` returns a new instance with the flag changed.

*Alternative considered:* a monthend-owned configuration keyed by project id. Rejected — `billable` already lives on `Project` and drives the same task, so co-locating keeps the gate's inputs in one aggregate and avoids a second source of truth.

### Generation reads the flag at generation time (no retroactivity)
`MonthEndProjectSnapshot` gains `leistungsnachweisEnabled`; `MonthEndTaskPlanningService` changes the gate to `project.billable() && !activeLeadIds.isEmpty() && project.leistungsnachweisEnabled()`. Because the value is evaluated only during a generation run, toggling affects the next run only. Idempotent regeneration is unchanged: turning the flag off does not delete existing tasks, and turning it on adds the task on the next run.

*Alternative considered:* reconcile open `LEISTUNGSNACHWEIS` tasks immediately on toggle. Rejected as out of scope and more complex; batch-at-generation matches the existing generation model.

### First inbound adapter on the `project` BC; two-layer authorization
A new `ProjectResource` implements the generated `ProjectApi`:
- `GET /projects` → `GetLeadProjectsUseCase`, returns the caller's led projects (via the existing `ProjectRepository.findAllByLead`). The `PROJECT_LEAD` role gate alone is sufficient because results are already scoped to the caller.
- `PUT /projects/{projectId}/leistungsnachweis-enabled` → `SetLeistungsnachweisEnabledUseCase`, which loads the project, asserts `project.leads().contains(actorId)` (throwing an authorization error otherwise), applies `withLeistungsnachweisEnabled(enabled)`, and saves. The `PROJECT_LEAD` role gate is not enough here — a lead of project A must not toggle project B — so the per-project lead check is enforced in the application service.

This follows the established pattern (`@MegaRolesAllowed`, `AuthenticatedActorContext.userId()`) and the codebase convention that cross-project authorization is a domain/application concern, not just a role annotation.

### Persistence
`ProjectEntity` gains a `leistungsnachweis_enabled` boolean column (`nullable = false`). A Liquibase changelog adds it with `defaultValueBoolean: true` so existing rows backfill to enabled. The entity↔domain MapStruct mapper carries the field both ways.

### DTO shape
`GET /projects` returns items of `{ id, zepId, name, billable, leistungsnachweisEnabled }`. `billable` is included so the UI can convey that the toggle has no effect on non-billable projects. `PUT` takes `{ enabled: boolean }`.

## Risks / Trade-offs

- **Default `false` would silently disable every billable project's proof-of-performance at the next month-end.** → Column defaults to `true` (opt-out) and the Liquibase change backfills existing rows to `true`.
- **A lead toggling another project's flag.** → Per-project `leads().contains(actorId)` check in the application service, not just the role gate.
- **Toggling mid-month is invisible until the next generation run** (could confuse users expecting immediate effect). → Accepted and documented as intended non-retroactive behaviour; the frontend can message this.
- **Introducing the first inbound adapter on the `project` BC** adds REST/OpenAPI surface where there was none. → Kept minimal (two endpoints) and consistent with existing resource conventions.

## Migration Plan

1. Add the Liquibase changelog (`leistungsnachweis_enabled` column, default `true`, not null) — backfills existing projects to enabled.
2. Deploy backend (domain gate, snapshot, endpoints). Behaviour is unchanged until a lead actively disables a project.
3. Frontend ships the configuration page consuming the new endpoints.

Rollback: the flag defaults to enabled, so reverting the code leaves generation behaving exactly as before; the extra column is inert if unused.

## Open Questions

None outstanding — default (opt-out), non-retroactivity, single-boolean scope, aggregate ownership, and the read+write surface were all settled during exploration.
