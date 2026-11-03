# Tasks

## 1. Project coordinator role (user BC)

- [x] 1.1 Add `PROJECT_COORDINATOR` to `shared.domain.model.Role` and to the `Role` enum in `src/main/resources/openapi/schemas/shared.yaml` (update its description); verify the generated model compiles with `mvn compile`.
- [x] 1.2 Add `ProjectCoordinatorEmails` value object in `user.domain.model` (case-insensitive, trimmed `contains`, mirroring `AdminEmails`) and `ProjectCoordinatorEmailsProducer` in `user.application` reading `mega.project-coordinator.emails` as `Optional<List<String>>`; add `project-coordinator.emails: "${MEGA_PROJECT_COORDINATOR_EMAILS:}"` to `application.yaml` and a test coordinator email to the `%test` profile. Verify with a unit test for the value object (match, whitespace/case, empty list).
- [x] 1.3 Extend `UserRolePolicyService.determineRoles` to grant `Role.PROJECT_COORDINATOR` on email match, independent of `PROJECT_LEAD`; verify with `UserRolePolicyServiceTest` cases for grant, revoke when email leaves the list, and no implied `PROJECT_LEAD`.
- [x] 1.4 Verify `GET /users/me` reports `PROJECT_COORDINATOR` with a REST test in the user resource test class.

## 2. Coordinators in month-end generation (monthend BC)

- [x] 2.1 Add `Set<UserId> findActiveCoordinatorIdsIn(YearMonth month)` to `MonthEndUserSnapshotPort` and implement it in `UserSnapshotAdapter` (users holding `PROJECT_COORDINATOR` and active in the month); verify with an adapter test covering active, inactive, and non-coordinator users.
- [x] 2.2 Change `MonthEndTaskPlanningService.planProjectTasks` to take `activeCoordinatorIds` and plan `LEISTUNGSNACHWEIS` only when billable flag enabled, leads non-empty, and coordinators non-empty, with coordinators as eligible actors; verify with `MonthEndTaskPlanningServiceTest` (coordinators eligible, leads not, no coordinators → no LN, no leads → no LN, REVIEW/ABRECHNUNG unchanged).
- [x] 2.3 In `GenerateMonthEndTasksService`, resolve coordinators once per run, pass them to planning, and log one ERROR per run when the set is empty; verify with `GenerateMonthEndTasksServiceTest` that other tasks are still created without coordinators and that a rerun with a coordinator adds only the missing LN tasks.

## 3. Bulk-completion authorization per type (monthend BC)

- [x] 3.1 Replace `MonthEndTaskRepository.existsLeadTask(month, projectId, leadId)` with `existsEligibleTaskOfType(month, projectId, type, actorId)` and implement it in `MonthEndTaskRepositoryAdapter`; verify with `MonthEndTaskRepositoryAdapterTest` (eligible on requested type → true, eligible only on another type → false, no tasks of type → false).
- [x] 3.2 Use the new check in `CompleteProjectLeadMonthEndTasksService` after the type validation; verify with `CompleteProjectLeadMonthEndTasksServiceTest` (coordinator completes LN, lead requesting LN rejected, coordinator requesting REVIEW on foreign project rejected, eligible with all done → empty result).
- [x] 3.3 Update REST tests for `POST /monthend/{month}/projects/{projectId}/tasks/complete` to the new `403`/`200 []` rules from the `monthend-rest-api` delta; verify the resource test class passes.
- [x] 3.4 Update the Javadoc of `MonthEndScope.LedProjects` to state that for tasks it covers every project on whose shared tasks the actor is eligible; add a `GetMonthEndStatusOverviewServiceTest` or repository test showing a coordinator's controlling overview includes LN projects they do not lead, with `canComplete` true only on their LN tasks (REST overview test if `canComplete` is evaluated there).

## 4. Project settings for coordinators (project BC)

- [x] 4.1 Rename `GetProjectSettingsUseCase.getLeadProjects(actorId)` to `getSettings()` and implement it over `projectRepository.findAll()` filtered by `isLeistungsnachweisConfigurableFrom(currentMonth)`; verify with `GetProjectSettingsServiceTest` (projects not led by the caller are included, existing billable/end-date filters still apply).
- [x] 4.2 Remove the `isLedBy` check from `SetLeistungsnachweisEnabledService` (keep actor id for the audit log); verify with its unit test that a non-lead coordinator can toggle and the non-billable rule still holds.
- [x] 4.3 Change `ProjectResource` to `@MegaRolesAllowed(Role.PROJECT_COORDINATOR)` and update the OpenAPI path docs in `openapi/paths/projects.yaml` to name the coordinator role; verify with `ProjectResourceTest` that coordinators get `200` and leads without the role get `403` on both endpoints.

## 5. Integration check

- [x] 5.1 Run `mvn test` and verify the full suite passes, including `ArchitectureTest`.
- [x] 5.2 Run `mvn package -Prewrite-check`; if it reports drift, fix it with `mvn rewrite:run` and re-run the check until the build passes.

## Workflow follow-up

- Set `MEGA_PROJECT_COORDINATOR_EMAILS` in every deployment environment before release.
- Coordinate release with the frontend change for the settings page and read-only LN columns for leads.
- Create the follow-up ticket for moving the email-configured roles to Keycloak.
