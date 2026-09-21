# Design

## Context

See proposal.md (Why) for motivation. Current state relevant to the approach:

**Project BC**
- `ProjectSettingsService` implements both `GetLeadProjectsUseCase` and `SetLeistungsnachweisEnabledUseCase`. Every other hexagon application service implements exactly one use case.
- `ProjectRestMapper` maps `Project` to a flat `ProjectItemDto`.
- The write path loads the project via `findAllByIds(Set.of(id)).getFirst()` and checks `project.leads().contains(actorId)` in the service.
- `ProjectResource` checks `enabled == null` by hand, although the generated `ProjectApi` already declares `@Valid @NotNull` on the body, the DTO getter is `@NotNull`, and `quarkus-hibernate-validator` is on the classpath. The check is unreachable.

**Project aggregate**
- `leistungsnachweisEnabled` is independent of `billable`.
- `create()` sets it to `true`, and `withSyncedZepData()` preserves it.
- `015-disable-leistungsnachweis.yaml` added the column with default `true`, so every stored project, including non-billable ones, holds `true`.

**Month-end generation**
- `MonthEndTaskPlanningService` creates a `LEISTUNGSNACHWEIS` task when `leistungsnachweisEnabled && billable && !activeLeads.isEmpty()`.
- The scheduler generates for the **current calendar month** on its last working day.

**Mapping the shared references to REST DTOs**
- `ProjectRef`/`UserRef` are mapped to DTOs in more than one mapper:
  - `MonthEndRestMapper` builds `zepUrl` from a `@Context ZepConfig` threaded through five methods, plus `@AfterMapping`. `MonthEndResource` injects the legacy `ZepConfig` only to pass it on.
  - `WorkTimeRestMapper` maps both references with plain MapStruct and never sets `zepUrl`. The generated `WorkTimeRestMapperImpl` confirms it, so every worktime entry sends `zepUrl: null`.
- `ZepConfig` lives in the legacy package `com.gepardec.mega.application.configuration`.

**Why `zepUrl` can never be null for a project**
- `projects.zep_id` is `NOT NULL UNIQUE`.
- `zepId` is a primitive `int` all the way through `ProjectEntity`, `Project` and `ProjectRef`.
- `ZepConfig.buildProjectUrl(int)` concatenates a required config URL with a fixed path, so it can't return null.

**Existing tests that need changing**
- `MonthEndRestMapperTest` and `WorkTimeRestMapperTest` create mappers via `Mappers.getMapper(...)`, without CDI.
- `MonthEndTaskPlanningServiceTest.planProjectTasks_shouldNotCreateLeistungsnachweis_whenProjectIsNonBillable` builds a snapshot with `billable=false, leistungsnachweisEnabled=true`. That state becomes impossible under the new rule.

## Goals / Non-Goals

**Goals:**
- Make "Leistungsnachweis requires billability" a single rule owned by the `Project` aggregate. Month-end generation and the REST layer only consume the result.
- Map `ProjectRef`/`UserRef` to their DTOs in exactly one place, and isolate the legacy `ZepConfig` dependency there.
- Keep the read side simple: the read use case still returns `Project` aggregates, and the composed `ProjectSettings` shape exists only in the REST contract.

**Non-Goals:**
- A domain-level `ProjectSettings` value object. There is one setting today.
- A database check constraint for the rule.
- Changing the `400` body produced by request validation. The legacy `ConstraintViolationExceptionMapper` returns an array of `{property, message}` instead of `ApiError`. This affects every endpoint and needs its own change.
- Composing `MonthEndProjectSnapshot` from `ProjectRef`.
- Frontend changes. A separate follow-up in `mega-frontend-v2` will handle them.

## Decisions

### D1: The billability rule lives in the `Project` aggregate
The rule is enforced in the aggregate at three points:
- **Constructor guard:** `leistungsnachweisEnabled && !billable` throws `IllegalArgumentException`. This is a last line of defense against corrupt persisted data or programming errors, so a `500` is the appropriate outcome.
- **`create(id, profile)`:** sets the flag to `profile.billable()`.
- **`withSyncedZepData(profile)`:** derives the flag from the old and new billability, following the transition table in the `project-aggregate` delta spec:
  - stays billable: keep the flag
  - becomes non-billable: `false`
  - becomes billable again: `true`

`withLeistungsnachweisEnabled(true)` on a non-billable project throws a new `LeistungsnachweisNotApplicableException extends ProjectException` **before** constructing anything. The existing `ProjectDomainExceptionMapper` already maps non-not-found `ProjectException`s to `400 ApiError`, and the contract already declares `400`, so the contract needs no change. `withLeistungsnachweisEnabled(false)` is always allowed.

*Alternatives considered:*
- **Store the lead's preference and derive the effective value (`billable && preference`).** This keeps an opt-out across a non-billable period. It was rejected by explicit decision: the stored state should say exactly what generation will do.
- **Silently force `false` on an enable request for a non-billable project.** Rejected. It hides a client error, and an explicit `400` is clearer.

### D2: Month-end checks the flag only
The `LEISTUNGSNACHWEIS` condition becomes `leistungsnachweisEnabled && !activeLeadIds.isEmpty()`. `billable` stays on `MonthEndProjectSnapshot`, because `ABRECHNUNG` still depends on it. Observable generation behavior is unchanged, because D1 guarantees that a project with the flag enabled is billable. The generation-time snapshot rule is untouched: a flag change caused by sync reaches only later generation runs.

*Alternatives considered:*
- **Keep `billable &&` as a defensive check.** Rejected. It keeps two owners of the same rule, which is what this change removes.
- **Add the invariant guard to `MonthEndProjectSnapshot` as well.** Rejected for the same reason. The snapshot is always derived from a valid `Project`.

### D3: Filter the settings list in the application service, with a domain predicate
- `GetProjectSettingsService` loads `projectRepository.findAllByLead(actorId)`.
- It filters with a new domain method: `Project.isLeistungsnachweisConfigurableFrom(YearMonth month)`, defined as `billable && (endDate == null || !endDate.isBefore(month.atDay(1)))`.
- `month` is `YearMonth.now(clock)`, using the injected `Clock` (the `ClockProducer` already exists).

The predicate includes projects starting in the future. It deliberately ignores assigned employees: those come from ZEP at generation time and aren't known in advance.

*Alternatives considered:*
- **A dedicated repository query.** Rejected. It would push a business rule into JPQL, and the lead's project set is small.
- **Computing the exact month the next run targets (the current month until its last working day, then the next month).** Rejected. It would copy month-end scheduling knowledge into the project BC. The simple rule over-includes projects that end this month only during the few days after the last working day.

### D4: One service per use case, plus renames
- `ProjectSettingsService` is split into `GetProjectSettingsService` (implements `GetProjectSettingsUseCase`, renamed from `GetLeadProjectsUseCase`; injects `ProjectRepository` and `Clock`) and `SetLeistungsnachweisEnabledService` (implements `SetLeistungsnachweisEnabledUseCase`; injects `ProjectRepository`).
- The split matches the codebase convention and the one-handler-per-use-case structure of the hexagonal guidelines. The different dependencies of the two services confirm they are separate.

Write-path flow:
1. `projectRepository.findById(id)`: an empty result throws `ProjectNotFoundException`, which becomes `404`.
2. `project.isLedBy(actorId)`: if false, throw `ForbiddenException`, which becomes `403`.
3. `project.withLeistungsnachweisEnabled(enabled)`: may throw, which becomes `400`.
4. `saveAll(List.of(updated))`.
5. `Log.infof` with actor, project and old → new value.

Authorization comes before the domain rule, so a non-lead learns nothing about the project's billability.

`ProjectRepository` gains `Optional<Project> findById(ProjectId)`. `findAllByIds` stays; it is used elsewhere.

### D5: Contract shape
In `schemas/projects.yaml`:
- `ProjectItem` is replaced by `ProjectSettings`: `additionalProperties: false`, required `project` (plain `$ref` to `shared.yaml#/ProjectRef`, non-nullable) and `leistungsnachweisEnabled`.
- `LeistungsnachweisToggleRequest` is unchanged.

In `paths/projects.yaml`, `/projects` becomes `/projects/settings` with operationId `getProjectSettings`, returning an array of `ProjectSettings`. In `openapi.yaml`, the path entry and the `components.schemas` registration are updated. There is no conflict with `PUT /projects/{projectId}/leistungsnachweis-enabled`, because the paths have different segment counts.

In `schemas/shared.yaml`, `ProjectRef.zepUrl` drops `nullable: true` and stays in `required`. `UserRef` is unchanged.

`ProjectRestMapper` maps `Project` → `ProjectSettingsDto` with `project` sourced from the whole `Project`. MapStruct composes `Project` → `ProjectRef` (a declared mapping method) → `ProjectRefDto` (the shared mapper). No hand-written mapping is needed. If MapStruct's two-step method selection doesn't resolve the chain, an explicit `ProjectRef toRef(Project)` method plus `@Mapping(target = "project", source = ".")` does.

### D6: One shared REST reference mapper
A new MapStruct mapper, `SharedRefRestMapper`, lives in `hexagon/shared/adapter/inbound/rest/`:
- It is an abstract class (`componentModel = JAKARTA`) with `ZepConfig` injected.
- `ProjectRefDto toDto(ProjectRef)` always sets `zepUrl = zepConfig.buildProjectUrl(zepId)`.
- `UserRefDto toDto(UserRef)` sets `zepUrl` only when `zepUsername` is present.

`MonthEndRestMapper`, `WorkTimeRestMapper` and `ProjectRestMapper` declare `uses = SharedRefRestMapper.class`:
- `MonthEndRestMapper` loses its own ref methods, both `@AfterMapping` ref enrichers, and every `@Context ZepConfig` parameter.
- `MonthEndResource` stops injecting `ZepConfig`.
- `WorkTimeRestMapper` loses its own ref methods, which fixes the null `zepUrl` bug.

This fulfils the `hexagon-boundary-conventions` requirement that legacy dependencies be isolated behind shared boundary components. ArchUnit already permits MapStruct mappers in `..hexagon..adapter..`.

For unit-testability without CDI, the BC mappers use `injectionStrategy = InjectionStrategy.CONSTRUCTOR`. Tests then build them explicitly around a shared-mapper instance that has a mocked `ZepConfig`. The exact wiring of `ZepConfig` into the abstract mapper (field or setter) is an implementation detail; it must be settable from tests.

*Alternatives considered:*
- **Keep `@Context ZepConfig` and fix only `WorkTimeRestMapper`.** Rejected. It adds a third copy of the ref mapping and leaves the root cause in place.
- **A hand-written CDI bean.** Rejected. The project rule is MapStruct for all mapping.

### D7: Drop the manual null check and prove validation with a test
- Remove the `enabled == null` branch in `ProjectResource`.
- A `@QuarkusTest` REST-Assured test sends `{"enabled": null}` and `{}`. It asserts `400` and that the use case was never invoked.
- It asserts the status only, because the body shape is the out-of-scope validation mapper issue.

### D8: Correct the stored data by editing 015 in place
The changeset has not been applied to any environment. In `015-disable-leistungsnachweis.yaml`:
- Keep the `addColumn` (`NOT NULL`, default `true`, which backfills).
- In the same changeset, add an `update` that sets `leistungsnachweis_enabled = false` where `billable = false`.
- Also remove the `= true` field initializer on `ProjectEntity`: the mapper always writes the value, and the initializer contradicts the rule.

*Alternative considered:* a new 016 changeset. It is unnecessary because 015 was never applied.

## Risks / Trade-offs

- **[Local dev databases that already ran 015 fail the Liquibase checksum at startup.]** → Only developer databases can be affected (no shared environment ran it). Reset the local database or clear the checksums once. H2 test databases are recreated on every run.
- **[A lead's opt-out is forgotten across a non-billable period.]** → Accepted by decision. The project reappears in the lead's settings list with the toggle on, so it is visible.
- **[The settings list over-includes projects ending this month during the days after the last working day.]** → Accepted. Toggling them only affects a manual regeneration of the current month.
- **[Listed projects may still produce no Leistungsnachweis tasks if nobody is assigned in ZEP.]** → Accepted. Assignments are only known at generation time.
- **[Month-end relies on the project invariant.]** → The constructor guard and the data correction make "enabled but non-billable" impossible to store or load. Month-end snapshots are only derived from `Project`.
- **[Switching mappers to constructor injection changes how existing mapper unit tests are set up.]** → Contained to `MonthEndRestMapperTest`, `WorkTimeRestMapperTest` and the project mapper tests. The shared mapper gets its own test for the URL rules.
- **[The contract change breaks the frontend's generated client.]** → Both sides are unreleased on the feature branch. The frontend follow-up regenerates the client and adapts the settings page. `ProjectRef.zepUrl` becoming non-null only tightens a TypeScript type.

## Migration Plan

1. Ship the backend change. The edited 015 changeset runs on first startup in each environment: it adds the column and disables the flag for non-billable projects.
2. Ship the frontend follow-up, which consumes `GET /projects/settings`.
3. Rollback: reverting the code is safe. Under the old condition (`enabled && billable`), non-billable projects never produced a Leistungsnachweis anyway, so the corrected data changes nothing.
