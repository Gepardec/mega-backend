## Context

The current hexagon user domain persists a provider-shaped snapshot in `hexagon_users`: roles are stored as a JSON set, ZEP data is stored as a serialized `ZepProfile`, Personio data is stored as a serialized `PersonioProfile`, and user activity is tracked as a separate status flag. The sync service then reconstructs the aggregate from those snapshots and mutates it in place during reconciliation.

That shape creates several problems:
- The local user aggregate owns more provider detail than the local domains actually need.
- `OFFICE_MANAGEMENT` assignment is wrong because the current sync compares configured email addresses to ZEP usernames.
- The user sync use case is manually instantiated from the scheduler instead of behaving like the CDI-managed application services used in `monthend` and `worktime`.
- `monthend` and `worktime` consume `User` through provider-shaped state such as `user.getZepProfile().username()` instead of explicit local fields or query projections.
- The aggregate model is mutable and inconsistent with the record-oriented style already used across most of the hexagon domain.

Additional constraints discovered during exploration:
- `ZepEmployeePort.fetchAll()` returns all employees in ZEP, not only currently active ones.
- Sync must still filter employees without email to avoid creating unusable local users.
- Historical employees must remain available locally so month-based use cases can resolve past activity from stored employment periods.
- Regular working times are used primarily by detailed working-time calculations and, in the legacy code, are fetched only for single-user lookups rather than bulk employee sync.

## Goals / Non-Goals

**Goals:**
- Redefine the hexagon `User` aggregate around locally owned identity, authorization, and employment-history data.
- Replace serialized JSON snapshots with normalized persistence for roles and employment periods.
- Derive current or month-specific activity from employment periods instead of persisting redundant user status.
- Persist only a stable Personio reference and move provider-owned details behind on-demand lookup ports.
- Keep `monthend` and `worktime` behaviorally stable while moving them off provider-shaped aggregate access.
- Align user sync with the existing CDI, transaction, and logging style of other hexagon application services.
- Move the user domain model to an immutable, record-oriented style.

**Non-Goals:**
- Redesign existing `monthend` or `worktime` REST payloads.
- Migrate the legacy layered `Employee` domain in the same change.
- Introduce full caching or replication of all provider-owned user detail.
- Solve the broader authenticated actor standardization already covered by `standardize-hexagon-shared-boundaries`.

## Decisions

### 1. Redefine `User` as a local aggregate, not a persisted provider snapshot

The hexagon `User` aggregate will store only local user data and stable external references:
- `UserId`
- `Email`
- `FullName`
- `ZepUsername`
- nullable `PersonioId`
- `EmploymentPeriods`
- `Set<Role>`

Full `ZepProfile` and `PersonioProfile` objects will no longer be part of the persisted aggregate state.

Why this decision:
- It makes the local user model reflect what the hexagon actually owns.
- It removes provider-only fields that are not broadly used by downstream domains.
- It makes the aggregate shape easier to consume from `monthend`, `worktime`, and future domains.

Alternatives considered:
- Keep nested provider profiles and merely stop reading some fields: rejected because the persisted model would still advertise the wrong ownership boundary.
- Replace the profiles with one large “synced user snapshot” value object: rejected because it still centralizes provider detail that should remain outside the aggregate.

The external references will be modeled as dedicated value objects rather than raw `String` / `int` fields. `ZepUsername` is the preferred name over `ZepId` because the ZEP reference used by the user domain is a username, not a numeric identifier.

### 2. Normalize employment periods and roles; derive activity instead of storing status

The persistence model will be split into:

| Structure | Purpose |
| --- | --- |
| `hexagon_users` | Core local identity and provider reference columns |
| `hexagon_user_roles` | One row per user-role assignment |
| `hexagon_user_employment_periods` | One row per employment period |

User activity will be derived from `EmploymentPeriods` for a given `LocalDate` or `YearMonth`. The explicit `ACTIVE` / `INACTIVE` user status column will be removed.

Why this decision:
- Activity is already temporal and naturally derived from employment history.
- ZEP returns all employees, so keeping historical users locally is compatible with sync input.
- Roles and employment periods are first-class domain concepts, unlike opaque provider snapshots.

Alternatives considered:
- Keep JSONB for roles and employment periods: rejected because it hides structured domain data inside persistence blobs and keeps query paths awkward.
- Keep a separate status column in addition to periods: rejected because it duplicates state and introduces drift between “status” and real employment history.

### 3. Sync all ZEP employees with email, regardless of current activity

Routine sync will process every ZEP employee returned by `fetchAll()` whose email is non-null. Ended employment periods will no longer cause a user to be filtered out or “disabled” during sync. Instead, the full employment period set will be updated locally and downstream use cases will derive activity when needed.

Why this decision:
- It preserves historical users for month-based use cases.
- It matches the actual semantics of the ZEP source response.
- It makes “active” a query concern instead of a sync lifecycle concern.

Alternatives considered:
- Continue filtering out users without an active period today: rejected because it loses historical employees needed for retrospective workflows.
- Persist every employee regardless of email: rejected because the current system still depends on email as the stable authentication and integration anchor.

### 4. Persist only `PersonioId`; fetch Personio-owned details on demand

Routine user sync will resolve and store only a stable Personio reference. It will call Personio by email only when a synced user does not yet have a stored `personioId`. Once present, routine sync will skip Personio for that user. Personio-owned details such as vacation balance and credit-card information will be fetched on demand through a dedicated provider-detail port.

Why this decision:
- The current hexagon code does not actually consume persisted Personio detail outside sync tests.
- It avoids turning sync into an always-on replication process for data the local aggregate does not own.
- It keeps provider detail fresh for the use cases that truly need it.

Alternatives considered:
- Keep persisting the full `PersonioProfile`: rejected because the data is provider-owned and currently unused by the local domains.
- Always re-query Personio during sync even when an ID is known: rejected because it adds routine network work without improving the local model.

### 5. Do not persist regular working times as part of user sync

Regular working times will be removed from routine user sync and from the persisted user aggregate. Use cases that need them will resolve them on demand from ZEP using stored `zepUsername`, behind a dedicated provider-detail port.

Why this decision:
- Legacy code already treats regular working times as single-user lookup detail, not bulk employee-sync material.
- The current hexagon use cases do not require regular working times for their persisted user read models.
- It keeps the sync focused on stable local user state and avoids unnecessary sync-time provider calls.

Alternatives considered:
- Persist regular working times in normalized child tables during sync: rejected because there is no current hexagon use case that justifies the extra sync and migration complexity.
- Keep them inside a serialized provider snapshot: rejected for the same ownership-boundary reasons as the other provider blobs.

### 6. Keep the existing office-management config property

The `OFFICE_MANAGEMENT` role input will continue to use the existing `mega.mail.reminder.om` configuration property. This change will correct the lookup semantics so the configured values are treated as email addresses, but it will not rename or relocate the property.

Why this decision:
- The user explicitly wants the configuration to stay as-is.
- The architectural problem is the wrong identifier comparison, not the location of the property itself.
- Keeping the property stable reduces rollout and deployment churn for this refactor.

Alternatives considered:
- Move to a dedicated user-sync-owned property: rejected because it adds migration work without solving the underlying role-assignment bug.
- Support both old and new properties during a compatibility window: rejected because it adds complexity without a concrete benefit.

### 7. Expand sync result reporting to reflect the real outcomes of a run

`UserSyncResult` will report:
- `added`
- `updated`
- `unchanged`
- `skippedNoEmail`
- `personioLinked`

`personioLinked` is a supplemental metric that records how many users received a Personio reference in the run; it may overlap with `added` or `updated`.

Why this decision:
- The old `disabled` count no longer matches the new sync semantics.
- The user asked for a result that reflects what actually happened during the sync.
- The richer summary is more useful for scheduler logs and operational checks.

Alternatives considered:
- Keep only `added`, `updated`, and `skipped`: rejected because it loses useful signal about unchanged runs and selective Personio linking.
- Replace the result with only one total processed count: rejected because it would hide the operational detail that made the result useful in the first place.

### 8. Make user sync a CDI-managed application service and transaction boundary

`SyncUsersUseCase` will be implemented by a CDI-managed application service that owns logging and the transaction boundary, similar to `GenerateMonthEndTasksService` and other existing hexagon application services. `SyncScheduler` will inject the use case rather than constructing it manually.

Why this decision:
- It aligns the user domain with the rest of the hexagon service style.
- It makes transaction and logging behavior explicit at the application-service layer.
- It removes wiring logic from the scheduler adapter.

Alternatives considered:
- Keep the current manually instantiated POJO service: rejected because it diverges from established patterns and hides lifecycle concerns in the adapter.
- Push transactions down into repository adapters only: rejected because the intended boundary is the use case, not each repository call.

### 9. Move consuming domains onto explicit local user fields and projections

`monthend`, `worktime`, and any other consuming domain will stop navigating provider-shaped state on `User`. They will instead consume direct local fields (`zepUsername`, `EmploymentPeriods`, etc.) or domain-specific query projections built from those fields.

Why this decision:
- It prevents other domains from depending on the old provider-shaped aggregate structure.
- It keeps downstream models aligned with the new ownership boundaries.
- It matches the explicit snapshot/query style already used elsewhere in the hexagon.

Alternatives considered:
- Preserve `getZepProfile()` as a compatibility wrapper: rejected because it would prolong the misleading aggregate shape and encourage new dependencies on it.

### 10. Model `User` as an immutable, record-oriented domain type

The user domain model will follow the same style as other hexagon domain models: a record-oriented aggregate with invariant checks and state-transition methods returning new instances rather than mutating in place.

Why this decision:
- It makes the user domain consistent with `monthend` and much of `worktime`.
- It reduces hidden state changes during sync and reconciliation.
- It fits the smaller, locally owned aggregate shape introduced by this change.

Alternatives considered:
- Keep the mutable class and only rename methods: rejected because the current mutability is part of the inconsistency the change is intended to remove.

## Risks / Trade-offs

- [Risk] Migrating from JSONB snapshots to normalized tables could lose historical data if the backfill is incomplete. -> Mitigation: introduce additive schema changes first, backfill from existing JSON columns, verify reads against real data, and drop obsolete columns only after code has switched.
- [Risk] On-demand provider lookups can increase latency for future use cases that need Personio detail or regular working times. -> Mitigation: keep the provider-detail ports narrow, add targeted caching later only if a concrete use case demonstrates the need.
- [Risk] Removing `status` could break callers that implicitly assume a stored active flag. -> Mitigation: replace those reads with explicit `EmploymentPeriods`-based activity checks and cover them in delta specs and tests.
- [Risk] Existing monthend and worktime code currently traverses `user.getZepProfile()`, so the refactor touches several modules at once. -> Mitigation: migrate query adapters and projections in the same change before removing the old model shape.
- [Risk] The office-management config currently lives under a mail reminder namespace, which is semantically confusing for authorization. -> Mitigation: decide during implementation whether to rename the config key in this change or keep a compatibility bridge and document the debt.

## Migration Plan

1. Add new schema elements for core user columns, role rows, and employment period rows, plus a nullable `personio_id` column.
2. Backfill normalized role and employment period data from the current JSONB columns where present.
3. Switch the user aggregate, mappers, and repository adapter to the new local model.
4. Convert `SyncUsersUseCase` into an injected CDI application service with transaction and logging boundaries.
5. Update sync behavior to filter only by email, persist employment history, resolve missing `personioId`, and stop persisting provider snapshots.
6. Update `monthend` and `worktime` adapters/services to consume direct user fields or new query projections instead of `zepProfile`.
7. Introduce provider-detail lookup ports for Personio-owned detail and regular working times.
8. Remove obsolete JSON and status columns after the new read path has been verified in tests and local data migration checks.

Rollback strategy:
- Keep the migration additive until the new code path is verified.
- Delay destructive column removal to the last implementation slice so the previous code path can be restored if needed.

## Open Questions

- None currently.
