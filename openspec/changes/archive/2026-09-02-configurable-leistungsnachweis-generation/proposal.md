## Why

Today a `LEISTUNGSNACHWEIS` month-end task is generated for every billable project that has active leads — project leads have no way to suppress it for projects where the proof-of-performance workflow does not apply. Project leads need a per-project switch to opt out of `LEISTUNGSNACHWEIS` generation.

## What Changes

- The `Project` aggregate gains a `leistungsnachweisEnabled` boolean, **defaulting to `true`** (opt-out semantics — existing behaviour is preserved for all current projects).
- ZEP resync preserves the flag (it is MEGA-managed, like project leads); it is never overwritten from ZEP.
- `MonthEndProjectSnapshot` carries the flag into the month-end context.
- Month-end task generation gates `LEISTUNGSNACHWEIS` on the flag: a task is created only when the project is billable, has active leads, **and** `leistungsnachweisEnabled` is `true`. The flag is read at generation time, so a toggle change only affects the next generation run (no retroactive add/removal of already-generated tasks).
- A new inbound REST surface on the `project` bounded context (its first inbound adapter):
  - `GET /projects` — returns the authenticated project lead's own projects with their `leistungsnachweisEnabled` state, so the frontend can render an editable toggle page.
  - `PUT /projects/{projectId}/leistungsnachweis-enabled` — sets the flag. Authorized by the `PROJECT_LEAD` role **and** a per-project check that the caller is a lead of that specific project.

## Capabilities

### New Capabilities
- `project-rest-api`: inbound REST endpoints on the project bounded context for a project lead to read their led projects and toggle per-project `LEISTUNGSNACHWEIS` generation, with role-based and per-project lead authorization.

### Modified Capabilities
- `project-aggregate`: the `Project` aggregate and its `MonthEndProjectSnapshot` read model gain a `leistungsnachweisEnabled` flag (default `true`), preserved across ZEP resync and lead sync.
- `monthend-task-generation`: `LEISTUNGSNACHWEIS` generation is additionally gated on the project's `leistungsnachweisEnabled` flag.

## Impact

- `Project` (project domain aggregate) and `ZepProjectProfile` interaction (flag preserved, not sourced from ZEP)
- `MonthEndProjectSnapshot` (month-end read model) + its mapper
- `MonthEndTaskPlanningService` (generation gating logic)
- `ProjectEntity` + new Liquibase changelog (new `leistungsnachweis_enabled` column, default `true`)
- New `ProjectResource` inbound adapter, `GetLeadProjectsUseCase` / `SetLeistungsnachweisEnabledUseCase` and application services in the project BC
- OpenAPI schema (new project read DTO + toggle request DTO, generated `ProjectApi`)
- Frontend: new project-lead configuration page consuming the two endpoints
