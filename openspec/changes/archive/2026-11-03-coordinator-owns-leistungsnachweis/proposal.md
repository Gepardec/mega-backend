# Proposal

## Why

Today the Leistungsnachweis is the project leads' job: they are the eligible actors on every `LEISTUNGSNACHWEIS` month-end task and the only ones who can switch Leistungsnachweis generation on or off. In practice billing is owned by the Projektkoordinator, who prepares the Leistungsnachweis for every project, while project leads only review and do the Abrechnung. MEGA should assign the work to the people who actually do it.

## What Changes

- Introduce a **project coordinator** role, assigned during user sync from a configured email list (same mechanism as office management and admin). One or more coordinators may be configured.
- **BREAKING** `LEISTUNGSNACHWEIS` tasks are generated with the active project coordinators as their only eligible actors (any one may complete). Project leads are no longer eligible on them. `PROJECT_LEAD_REVIEW` and `ABRECHNUNG` stay with the project leads.
- Leistungsnachweis generation keeps its existing gates (billable, flag enabled, at least one active lead). If no active coordinator exists at generation time, all other tasks are still generated, no `LEISTUNGSNACHWEIS` task is created, and an error is logged. A later run after the configuration is fixed creates the missing tasks.
- The coordinator acts in the existing controlling capacity. Being a project lead is assumed of the coordinator, not enforced. Because the coordinator is eligible on Leistungsnachweis tasks of every project, their controlling overview covers those projects; only the Leistungsnachweis tasks are completable for them there.
- **BREAKING** Project-lead bulk completion is authorized only when the caller is eligible on at least one task **of the requested type** for that project and month. A caller with no such task now receives `403` instead of `200` with an empty result.
- **BREAKING** `GET /projects/settings` and `PUT /projects/{projectId}/leistungsnachweis-enabled` are restricted to the project coordinator role and cover all billable, not-yet-ended projects instead of only the caller's led projects. Project leads without the coordinator role receive `403`. The frontend handles the transition.
- Already generated tasks are not changed: existing `LEISTUNGSNACHWEIS` tasks keep their project leads as eligible actors. The change takes effect from the next generation run.
- Out of scope: moving the email-configured roles (office management, admin, project coordinator) to Keycloak — tracked as a follow-up ticket. Clarification rights are unchanged; they keep following the project's leads.

## Capabilities

### New Capabilities
_None._

### Modified Capabilities
- `user-role-policy`: role policy assigns the project coordinator role from a configured coordinator email list.
- `user-aggregate`: the closed role set includes the project coordinator role.
- `user-rest-api`: `GET /users/me` may report the project coordinator role.
- `monthend-task-generation`: `LEISTUNGSNACHWEIS` eligible actors are the active project coordinators; missing coordinator skips Leistungsnachweis and logs an error.
- `monthend-task-aggregate`: Leistungsnachweis tasks are modeled with coordinators as eligible actors; coordinator changes after generation do not alter open tasks. Leistungsnachweis stays a controlling duty.
- `monthend-task-completion`: Leistungsnachweis completed by coordinators; bulk-completion authorization based on eligibility for the requested type.
- `monthend-rest-api`: bulk-completion `403` rule follows the requested type; empty-result case changes; coordinator uses the bulk endpoint for Leistungsnachweis.
- `monthend-status-overview`: controlling scope is defined by eligibility on any controlling-duty task; Leistungsnachweis tasks are completable for coordinators and read-only for leads.
- `project-rest-api`: settings endpoints restricted to the coordinator role and scoped to all qualifying projects.

## Impact

- **User BC**: new role value, coordinator email config property and producer, role policy rule. OpenAPI `Role` enum gains a value.
- **Monthend BC**: generation reads active coordinators through the monthend user snapshot outbound port; task planning assigns Leistungsnachweis eligibility to coordinators; bulk completion authorization query changes; controlling-scope wording.
- **Project BC**: settings read and toggle use cases lose the per-project lead check; REST resource role restriction changes.
- **Configuration**: new environment variable for coordinator emails; must be set in every environment that runs generation, or Leistungsnachweis tasks are skipped with an error.
- **Frontend**: settings page moves to coordinators; Leistungsnachweis columns become read-only for leads.
- **Rollout**: months generated before deployment keep leads as Leistungsnachweis actors until completed.
