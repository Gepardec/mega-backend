## Context

The month-end status overview currently reads relevant `MonthEndTask` records for an actor and month, resolves project snapshot data in bulk, and maps the result into overview entries containing task metadata, status, a nested project reference object, a flat `subjectEmployeeId`, and optional `completedBy` information.

That is sufficient for task tracking, but it is not sufficient for the UI, which needs a human-readable employee label for review and employee-owned tasks. The month-end hexagon already has a user snapshot query path through `MonthEndUserSnapshotPort`, but it currently exposes only the data needed for planning and validation flows and does not provide a targeted lookup or a display-ready full name for overview rendering.

This change touches the month-end application query path, the user snapshot outbound port and adapter, the status overview read model, and the generated REST contract. It benefits from an explicit design because the change is both cross-cutting and contract-breaking: the status overview should stop exposing a flat `subjectEmployeeId` and instead expose a nested subject employee reference object analogous to the existing project object.

## Goals / Non-Goals

**Goals:**
- Return subject employee display data in month-end status overview entries, including the employee's full name.
- Replace the flat overview `subjectEmployeeId` with a nullable nested subject employee reference object.
- Resolve subject employee display data explicitly in bulk for overview queries.
- Preserve the current DDD and hexagonal modeling choice that month-end tasks continue to store only `UserId` references.
- Keep `ABRECHNUNG` as the only status overview task type without a subject employee reference.

**Non-Goals:**
- Changing month-end task generation, completion, or clarification rules.
- Changing worklist response models or clarification response models in this change.
- Reworking `completedBy` into a nested object.
- Introducing JPA user relationships or ORM-driven joins from month-end task entities.
- Adding a separate denormalized read store for employee display data.

## Decisions

### Decision: Enrich status overview entries in the application query path through explicit bulk subject employee snapshot reads

`GetMonthEndStatusOverviewUseCase` will continue to load the relevant month-end tasks first, because tasks remain the source of truth for which obligations belong in the actor's overview. After loading tasks, the application service will collect the distinct non-null `subjectEmployeeId` values from those tasks and resolve the corresponding user snapshots in bulk through an outbound port before constructing the final overview entries.

This keeps the overview as an application-level query composition and avoids pushing display enrichment into persistence entities, REST adapters, or clients.

Alternatives considered:

- Resolve each subject employee one-by-one after reading tasks: rejected because it introduces avoidable N+1 lookup behavior.
- Let the UI or a REST adapter look up employee names separately: rejected because the employee label is part of the overview query model itself, not a transport-only concern.
- Build the overview from a custom persistence projection with user joins: rejected for now because the existing use case already centers on task obligations and does not need a dedicated read store.

### Decision: Reuse the existing month-end user snapshot read path and extend it with targeted lookup and display-ready full names

The user lookup path will continue to use `MonthEndUserSnapshot` as the read model exposed through `MonthEndUserSnapshotPort`. The snapshot will be extended with a display-ready `fullName`, and the port will add a targeted `findByIds(Set<UserId>)` operation so the overview query can request only the employees it needs while existing flows can keep using `findAll()`.

This keeps a single month-end-facing user read model for application services and avoids introducing a second, narrower summary type prematurely.

Alternatives considered:

- Reuse `findAll()` and filter in-memory in the overview service: rejected as the primary design because a targeted bulk lookup is more explicit and avoids loading unrelated users by default.
- Expose first and last name separately in the overview contract: rejected because the status overview is already a display-oriented query model and the UI needs a stable full-name label, not name-formatting rules.
- Introduce a separate `UserSummary` read model just for overview display: rejected because the existing snapshot type already represents month-end user query context and can absorb the display field cleanly.

### Decision: Replace the flat overview subject employee id with a nullable nested subject employee reference object

The month-end status overview entry returned by the application layer will replace the flat `subjectEmployeeId` field with a nullable value object, for example `MonthEndStatusOverviewSubjectEmployee`, containing the employee identifier and full name. The shared REST status overview response will expose the same nested structure so the UI can render employee labels without a follow-up lookup while keeping employee display data grouped together.

This keeps the contract consistent with the existing nested project reference and avoids duplicating the same employee identity in multiple shapes.

Alternatives considered:

- Keep `subjectEmployeeId` and add a parallel `subjectEmployeeName` string: rejected because it scatters one concept across multiple flat fields and scales poorly if more employee display fields are needed later.
- Add a nested `subjectEmployee` object but also keep `subjectEmployeeId`: rejected because it duplicates the same information and prolongs two competing contract shapes.
- Keep only the flat `subjectEmployeeId` and rely on client-side lookup: rejected because it does not solve the UI's immediate need for a human-readable employee label.

### Decision: Treat missing subject employee snapshots as inconsistent overview data, while preserving null only for task types that have no subject employee

If an overview task has a non-null `subjectEmployeeId`, the application service will expect a matching subject employee snapshot and treat a missing snapshot as an inconsistent read-model assembly, similar to the existing project snapshot lookup. `ABRECHNUNG` remains the only task type that legitimately yields no subject employee object.

This keeps the overview contract predictable: callers either receive a complete subject employee reference or, for the task types that do not have one by definition, no subject employee reference at all.

Alternatives considered:

- Return a partial nested object with only the id when the full name cannot be resolved: rejected because it reintroduces the original UI problem and hides data inconsistencies.
- Silently drop the subject employee object when a snapshot is missing: rejected because it makes required employee-linked tasks indistinguishable from task types that legitimately have no subject employee.

## Risks / Trade-offs

- [User snapshot lookups add an extra query step to the overview path] -> Use a bulk lookup by distinct non-null `UserId` values and keep the query scoped to overview employees only.
- [The REST response shape changes from `subjectEmployeeId` to `subjectEmployee`] -> Treat this as a coordinated breaking change and update generated models and consumers together.
- [Current employee names are not historical month-end snapshots] -> Accept current user metadata for this UI-oriented overview unless a later requirement demands point-in-time naming.
- [The overview read model becomes more display-oriented] -> Limit enrichment to the employee reference data required by the status overview and keep write-side models unchanged.

## Migration Plan

This is a contract migration with no database schema change.

1. Extend the month-end user snapshot read model and port to expose `fullName` and support bulk lookup for requested user ids.
2. Replace the flat `subjectEmployeeId` in the month-end status overview read model with a nullable nested subject employee reference object.
3. Enrich `GetMonthEndStatusOverviewService` to collect distinct non-null subject employee ids, resolve user snapshots in bulk, and map the full employee reference into each overview entry.
4. Update the canonical month-end OpenAPI document and generated models to replace the flat status overview `subjectEmployeeId` field with a nullable nested subject employee schema.
5. Update application tests, REST mapper tests, and REST/integration tests for the new overview shape and the breaking contract change.

Rollback strategy:

- restore the flat `subjectEmployeeId` field in the overview domain and REST models
- remove the added bulk user lookup and display-name enrichment from the overview path
- regenerate the previous shared response models from the restored OpenAPI contract

## Open Questions

None.
