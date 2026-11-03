# Design

## Context

See proposal.md for motivation. Current state relevant to the approach:

- **Roles** are derived in `UserRolePolicyService.determineRoles` from two config-backed value objects, `OfficeManagementEmails` (`mega.mail.reminder.om`) and `AdminEmails` (`mega.admin.emails`, optional list). `PROJECT_LEAD` is preserved there and reconciled separately by `SyncProjectLeadRolesService`.
- **Generation** (`GenerateMonthEndTasksService`) loads active users through `MonthEndUserSnapshotPort` (implemented by the monthend adapter `UserSnapshotAdapter` over the user BC's `UserRepository`), filters each project's leads to active users, and calls `MonthEndTaskPlanningService.planProjectTasks(month, project, activeLeadIds, assignedUsers)`, which uses `activeLeadIds` as eligible actors for `PROJECT_LEAD_REVIEW`, `LEISTUNGSNACHWEIS`, and `ABRECHNUNG`. Idempotency is by business key, so a rerun only adds missing tasks.
- **Controlling scope** (`MonthEndScope.LedProjects`, `LED_PROJECTS_QUERY`) and **bulk-completion authorization** (`MonthEndTaskRepository.existsLeadTask`) both mean "eligible on any task whose completion policy is `ANY_ELIGIBLE_ACTOR`", i.e. any of `LEISTUNGSNACHWEIS`, `PROJECT_LEAD_REVIEW`, `ABRECHNUNG`.
- **`canComplete`** = eligible ∧ `type.dutyOf() == capacity`. `LEISTUNGSNACHWEIS` stays a `CONTROLLING` duty, so no change is needed there.
- **Clarifications** take their lead snapshot from `MonthEndProjectContextService`, which uses the project's current leads, not task eligibility. They are unaffected.
- **Project settings**: `ProjectResource` is class-annotated `@MegaRolesAllowed(Role.PROJECT_LEAD)`; `GetProjectSettingsService.getLeadProjects` uses `findAllByLead`; `SetLeistungsnachweisEnabledService` checks `project.isLedBy(actorId)`.

## Goals / Non-Goals

**Goals:**
- Coordinator eligibility on newly generated `LEISTUNGSNACHWEIS` tasks, read cross-BC through the existing monthend outbound port.
- Bulk-completion authorization expressed per requested task type.
- Settings endpoints authorized by role only.

**Non-Goals:**
- No data migration of existing `LEISTUNGSNACHWEIS` tasks (generation-time snapshot rule).
- No enforcement that a coordinator holds `PROJECT_LEAD`.
- No change to clarification eligibility, capacities, task duties, or the overview response shape.
- No generalization of the email-list value objects; the Keycloak follow-up will replace all three.

## Decisions

### 1. New `Role.PROJECT_COORDINATOR`, configured like admin
Add `PROJECT_COORDINATOR` to `Role` and to the OpenAPI `Role` enum (`schemas/shared.yaml`). Add a `ProjectCoordinatorEmails` value object in `user.domain.model` and a `ProjectCoordinatorEmailsProducer` reading `mega.project-coordinator.emails` (`${MEGA_PROJECT_COORDINATOR_EMAILS:}`) as `Optional<List<String>>`, normalized like `AdminEmails`. `UserRolePolicyService.determineRoles` adds the role on match; it is email-derived only, so leaving the list revokes it on the next sync.

*Alternative*: a generic `ConfiguredEmails` VO shared by OM/admin/coordinator. Rejected for now: the Keycloak follow-up removes all three, so the refactor would be throwaway.

### 2. Monthend reads coordinators through `MonthEndUserSnapshotPort`
Add `Set<UserId> findActiveCoordinatorIdsIn(YearMonth month)` to `MonthEndUserSnapshotPort`, implemented in `UserSnapshotAdapter` by filtering users that hold `Role.PROJECT_COORDINATOR` and are active in the month. Keeps the cross-BC rule: monthend depends only on its own outbound port; the user BC's role is translated at the adapter.

*Alternative*: expose `roles` on the shared `UserRef` and filter in the application service. Rejected: widens a shared-kernel type used everywhere for one consumer.

### 3. Planning takes coordinators as an explicit input
`planProjectTasks(month, project, activeLeadIds, activeCoordinatorIds, assignedUsers)`. `LEISTUNGSNACHWEIS` is planned when `project.leistungsnachweisEnabled() && !activeLeadIds.isEmpty() && !activeCoordinatorIds.isEmpty()`, with `activeCoordinatorIds` as eligible actors. The planning service stays pure; it does not log.

`GenerateMonthEndTasksService` resolves coordinators once per run. When the set is empty it logs once per run at ERROR — `Log.errorf("No active project coordinator for %s; Leistungsnachweis tasks skipped", month)` — and still runs planning (which then simply plans no `LEISTUNGSNACHWEIS`). No exception, so the rest of generation is unaffected; the scheduled rerun or a manual trigger fills the gap through business-key idempotency.

*Alternative*: throw to abort generation. Rejected by the user (blocks every employee's time check).

### 4. Bulk-completion authorization per requested type
Replace `existsLeadTask(month, projectId, leadId)` with `existsEligibleTaskOfType(month, projectId, type, actorId)` and call it from `CompleteProjectLeadMonthEndTasksService` after the type check. The `400` for unsupported types still comes first; `403` now also covers "no task of that type" (spec'd). The controlling scope's task query keeps the any-shared-type rule, so coordinators fall into it for every project with their LN tasks (also for the payroll-month existence check, which uses the same scope). `MonthEndScope.LedProjects` keeps its name: it also drives the clarification query, which is based on the clarification's lead snapshot and is unaffected. Only its Javadoc is updated to say that for tasks it covers every project on whose shared tasks the actor is eligible.

*Alternative*: keep `existsLeadTask` and rely on per-task skipping. Rejected: a coordinator would pass authorization for lead review on foreign projects and get a misleading `200 []`.

### 5. Project settings: role-only authorization
- `ProjectResource` → `@MegaRolesAllowed(Role.PROJECT_COORDINATOR)`.
- `GetProjectSettingsUseCase.getLeadProjects(actorId)` → `getSettings()`: `projectRepository.findAll()` filtered by `isLeistungsnachweisConfigurableFrom(currentMonth)`. `ProjectRepository.findAllByLead` stays; the worktime project snapshot adapter still uses it.
- `SetLeistungsnachweisEnabledService` drops the `isLedBy` check; the actor id is kept only for the audit log line. Authorization is the resource's role check, consistent with other role-gated hexagon endpoints.

### 6. Spec drift fixed in passing
`user-rest-api` lists the role set without `ADMIN`, while the OpenAPI enum already declares it. The delta restores `ADMIN` alongside `PROJECT_COORDINATOR`.

## Risks / Trade-offs

- [Coordinator env var missing in an environment] → LN silently absent apart from the ERROR log. Mitigation: set `MEGA_PROJECT_COORDINATOR_EMAILS` in all deployment configs as part of rollout; add a coordinator to the `%test` profile so integration tests exercise the path.
- [Configured coordinator is not a project lead] → LN tasks are generated for them, but `CONTROLLING` reads and the bulk endpoint return `403` (they require `PROJECT_LEAD`). Accepted per decision; it is an organizational assumption.
- [Coordinator overview grows to all projects] → more rows in their controlling overview, including read-only lead tasks of foreign projects. Accepted; it is the visibility the role needs.
- [Mid-month rollout] → months already generated keep leads as LN actors; the coordinator only sees those projects they lead. Communicate at release; no migration.
- [Frontend still calls settings as a lead] → `403` until the frontend change ships. Ship together or frontend first behind the role check.

## Migration Plan

1. Configure `MEGA_PROJECT_COORDINATOR_EMAILS` per environment before deploying.
2. Deploy backend with the matching frontend change.
3. The next user sync (≤30 min) grants the role; the next generation run assigns LN to coordinators.
4. Rollback: redeploy the previous version. LN tasks generated in between keep coordinators as eligible actors (snapshot rule), so after a rollback leads cannot complete them and the old bulk check would still let coordinators who lead a project act. Avoid rolling back across a generation date; if unavoidable, those tasks have to be corrected in the database.
