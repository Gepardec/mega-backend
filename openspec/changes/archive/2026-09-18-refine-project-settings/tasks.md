# Tasks

## 1. Project aggregate: billability rule and domain methods

- [x] 1.1 Add `LeistungsnachweisNotApplicableException extends ProjectException` in `project/domain/error`, and verify
  the code compiles
- [x] 1.2 In `Project`, add the constructor guard (`leistungsnachweisEnabled && !billable` throws
  `IllegalArgumentException`) and set the flag in `create()` to `profile.billable()`. Verify with `ProjectTest` cases:
    - a billable profile creates the project with the flag enabled
    - a non-billable profile creates it with the flag disabled
    - constructing a non-billable project with the flag enabled throws
- [x] 1.3 In `Project.withSyncedZepData()`, derive the flag following the transition table (stays billable: keep;
  becomes non-billable: `false`; becomes billable again: `true`). Verify with a `ProjectTest` case per transition,
  including the case where a lead's opt-out on a billable project survives a billable resync
- [x] 1.4 Make `Project.withLeistungsnachweisEnabled(true)` throw `LeistungsnachweisNotApplicableException` on a
  non-billable project, and keep `withLeistungsnachweisEnabled(false)` always allowed. Verify with `ProjectTest` cases
  for enable-on-non-billable (throws) and disable-on-non-billable (flag stays false)
- [x] 1.5 Add `Project.isLedBy(UserId)` and `Project.isLeistungsnachweisConfigurableFrom(YearMonth)` (billable, and no
  end date or an end date not before the month's first day). Verify with `ProjectTest` cases:
    - lead and non-lead
    - non-billable
    - ended before the month (2026-08-31 vs 2026-09)
    - ends within the month (2026-09-10)
    - no end date
    - starts in a future month

## 2. Persistence

- [x] 2.1 Edit `015-disable-leistungsnachweis.yaml` in place: keep the `addColumn`, and add an `update` in the same
  changeset that sets `leistungsnachweis_enabled = false` where `billable = false`. Verify with
  `mvn test -Dtest=ProjectRepositoryAdapterTest` (H2 runs the changelog from scratch)
- [x] 2.2 Remove the `= true` field initializer on `ProjectEntity.leistungsnachweisEnabled`, and verify the
  `ProjectRepositoryAdapterTest` round-trip still passes for billable and non-billable projects
- [x] 2.3 Add `Optional<Project> findById(ProjectId)` to the `ProjectRepository` port and `ProjectRepositoryAdapter`.
  Verify with `ProjectRepositoryAdapterTest` cases for an existing and an unknown id
- [x] 2.4 Verify the sync effect of the rule with `SyncProjectsServiceTest` cases:
    - a stored billable project resynced as non-billable is saved with the flag disabled and counted as updated
    - a stored non-billable project resynced as billable is saved with the flag enabled

## 3. Month-end Leistungsnachweis condition

- [x] 3.1 Change the `LEISTUNGSNACHWEIS` condition in `MonthEndTaskPlanningService` to
  `leistungsnachweisEnabled && !activeLeadIds.isEmpty()`
- [x] 3.2 Update
  `MonthEndTaskPlanningServiceTest.planProjectTasks_shouldNotCreateLeistungsnachweis_whenProjectIsNonBillable` to use a
  non-billable snapshot with the flag disabled, the only state the project BC can produce. Verify
  `mvn test -Dtest=MonthEndTaskPlanningServiceTest` passes, with `ABRECHNUNG` still requiring billability

## 4. Shared REST reference mapper

- [x] 4.1 Create the MapStruct abstract class `SharedRefRestMapper` in `hexagon/shared/adapter/inbound/rest/`, with
  `ZepConfig` injected so that tests can set it:
    - `ProjectRef → ProjectRefDto` always sets `zepUrl` via `buildProjectUrl`
    - `UserRef → UserRefDto` sets `zepUrl` via `buildEmployeeUrl` only when `zepUsername` is present, else `null`

  Verify with a new `SharedRefRestMapperTest` covering all three cases
- [x] 4.2 Switch `MonthEndRestMapper` to `uses = SharedRefRestMapper.class` with constructor injection:
    - remove its own `ProjectRef`/`UserRef` mapping methods, both `@AfterMapping` ref enrichers, and every
      `@Context ZepConfig` parameter
    - drop the `ZepConfig` injection and arguments in `MonthEndResource`

  Verify that the adapted `MonthEndRestMapperTest` and `MonthEndResourceTest` pass with unchanged `zepUrl` assertions
- [x] 4.3 Switch `WorkTimeRestMapper` to `uses = SharedRefRestMapper.class` with constructor injection and remove its
  own ref mapping methods. Verify:
    - the adapted `WorkTimeRestMapperTest` asserts non-null project `zepUrl` and employee `zepUrl`
    - `WorkTimeEmployeeAndProjectLeadResourceTest` asserts `project.zepUrl` is populated in the work time report
      response

## 5. REST contract

- [x] 5.1 In `schemas/projects.yaml`, replace `ProjectItem` with `ProjectSettings`:
    - `additionalProperties: false`
    - required `project` (plain `$ref` to `shared.yaml#/ProjectRef`) and `leistungsnachweisEnabled`

  Update the `components.schemas` registration in `openapi.yaml`
- [x] 5.2 In `paths/projects.yaml`, change `/projects` to `/projects/settings` with operationId `getProjectSettings`,
  returning an array of `ProjectSettings`. Update the path entry in `openapi.yaml`
- [x] 5.3 In `schemas/shared.yaml`, remove `nullable: true` from `ProjectRef.zepUrl` (keep it required; leave `UserRef`
  unchanged). Verify with `mvn generate-sources`:
    - the bundled spec shows `ProjectRef.zepUrl` required and non-nullable
    - `ProjectSettingsDto` and `ProjectApi.getProjectSettings()` are generated
    - `ProjectItemDto` is gone

## 6. Project application services

- [x] 6.1 Rename `GetLeadProjectsUseCase` to `GetProjectSettingsUseCase`. Add `GetProjectSettingsService`, which injects
  `ProjectRepository` and `Clock` and returns the caller's led projects filtered by
  `isLeistungsnachweisConfigurableFrom(YearMonth.now(clock))`. Verify with `GetProjectSettingsServiceTest` using a fixed
  `Clock`:
    - non-billable projects are excluded
    - ended projects are excluded
    - a billable project with the flag disabled is included
    - a future project is included
- [x] 6.2 Add `SetLeistungsnachweisEnabledService`, which:
    - uses `findById`, throwing `ProjectNotFoundException` when the project is missing
    - checks `isLedBy`, throwing `ForbiddenException` for a non-lead before the domain rule is evaluated
    - applies `withLeistungsnachweisEnabled`, saves, and logs the change (actor, project, old and new value) through
      `io.quarkus.logging.Log`

  Verify with `SetLeistungsnachweisEnabledServiceTest` cases:
    - unknown project
    - non-lead (no save)
    - enable on a billable project
    - enable on a non-billable project throws `LeistungsnachweisNotApplicableException` (no save)
    - disable on a non-billable project succeeds
- [x] 6.3 Delete `ProjectSettingsService` and `ProjectSettingsServiceTest`, and verify no references remain
  (`grep -r ProjectSettingsService src` is empty)

## 7. Project REST adapter

- [x] 7.1 Update `ProjectRestMapper` to map `Project → ProjectSettingsDto`, with `project` composed via
  `SharedRefRestMapper` (`uses`, constructor injection). Verify with a `ProjectRestMapperTest`: the entry carries
  `project.id`, `project.name`, a non-null `project.zepUrl` and `leistungsnachweisEnabled`
- [x] 7.2 Update `ProjectResource` to implement `getProjectSettings()` via `GetProjectSettingsUseCase`, and remove the
  manual `enabled == null` check in `setLeistungsnachweisEnabled`. Verify that it compiles against the regenerated
  `ProjectApi`
- [x] 7.3 Extend `ProjectResourceTest` (REST-Assured):
    - `GET /projects/settings` returns `200` with the composed entry shape and no `zepId`/`billable` fields
    - a non-project-lead gets `403`
    - `PUT` with `{"enabled": null}` and with `{}` each return `400`, and the use case is never invoked
    - `PUT` where the use case throws `LeistungsnachweisNotApplicableException` returns `400`

## 8. Final verification

- [x] 8.1 Run `mvn clean package`, and verify that all tests pass, including `HexagonalArchitectureTest`, and that
  MapStruct reports no unmapped-target warnings for `zepUrl`
