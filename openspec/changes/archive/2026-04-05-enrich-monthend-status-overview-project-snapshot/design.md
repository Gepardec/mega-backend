## Context

The month-end status overview currently reads relevant `MonthEndTask` records for an actor and month and maps them directly into overview entries containing task metadata, status, `ProjectId`, and optional employee/completion references. That is sufficient for task tracking, but it is not sufficient for the UI, which needs at least the project name to render a usable matrix or dashboard.

The month-end hexagon already has a project snapshot query path used by application services that need project context. It currently exposes a reusable `MonthEndProjectSnapshot` read model through `MonthEndProjectSnapshotPort`, while `MonthEndTask` and `MonthEndClarification` persistence remain modeled by scalar `project_id` foreign keys instead of JPA object relationships.

This change touches the month-end application query path, the project snapshot outbound port and adapter, the status overview read model, and the generated REST contract. It benefits from an explicit design because the main question is architectural: enrich the query model without reshaping aggregate persistence around ORM navigation.

## Goals / Non-Goals

**Goals:**
- Return project display data in month-end status overview entries, starting with the project name.
- Resolve project data explicitly in bulk for overview queries.
- Preserve the current DDD and hexagonal modeling choice that month-end aggregates reference projects by `ProjectId`.
- Keep the REST contract additive by extending status overview entries with the new project display field.

**Non-Goals:**
- Introducing JPA `@ManyToOne` relationships from month-end task or clarification entities to project entities.
- Changing month-end task generation, task completion, or clarification business rules.
- Changing worklist response models in this change.
- Adding a new denormalized read store or database schema for project labels.

## Decisions

### Decision: Enrich status overview entries in the application query path through explicit bulk project snapshot reads

`GetMonthEndStatusOverviewUseCase` will continue to load the relevant month-end tasks first, because tasks remain the source of truth for which obligations belong in the actor's overview. After loading tasks, the application service will collect the distinct `ProjectId` values from those tasks and resolve the corresponding project snapshots in bulk through an outbound port before constructing the final overview entries.

This keeps the overview as an application-level query composition rather than pushing UI-driven data enrichment into persistence entities or REST adapters.

Alternatives considered:

- Resolve each project one-by-one after reading tasks: rejected because it introduces avoidable N+1 lookup behavior.
- Resolve project names in the REST adapter after the use case returns: rejected because project display context is part of the overview query model itself, not a transport-only concern.
- Build the overview from a custom persistence projection instead of tasks plus enrichment: rejected for now because the existing use case already centers on task obligations and does not need a dedicated read store.

### Decision: Reuse the existing project snapshot read model and extend it with the fields needed for display

The project lookup path will continue to use `MonthEndProjectSnapshot` as the read model exposed through `MonthEndProjectSnapshotPort`. The snapshot will be extended with `name`, and the port will support bulk lookup for a requested set of `ProjectId` values so the status overview query can request only the projects it needs.

This keeps a single month-end-facing project read model for application services and avoids creating a second, narrower projection type prematurely. It also leaves room to reuse additional snapshot fields later without reworking the port contract again.

Alternatives considered:

- Introduce a new `ProjectSummary`-style read model just for the overview: rejected because the existing snapshot type already represents month-end project query context and can absorb the display field cleanly.
- Reuse `findAll()` and filter in-memory in the status overview service: rejected as the primary design because a targeted bulk lookup is more explicit and avoids loading unrelated projects by default.

### Decision: Keep month-end persistence modeled by project identifiers instead of JPA project relationships

`MonthEndTask` and `MonthEndClarification` will continue to reference projects by `ProjectId`, and their JPA entities will continue to persist a scalar `project_id` foreign key. The new project display data will be obtained through the project snapshot port, not through ORM navigation.

This keeps aggregate boundaries explicit, preserves the simple ID-based MapStruct mapping on the write path, and avoids persistence behavior that depends on hidden lazy loading, join choices, or entity reference management.

Alternatives considered:

- Add JPA project relationships on month-end entities and join-fetch the project name: rejected because it increases ORM coupling in infrastructure, complicates entity mapping and update flows, and solves a read-model problem by reshaping aggregate persistence.

### Decision: Extend the overview read model and REST contract with a nested project reference object

The month-end status overview entry returned by the application layer will expose a small project reference value object that contains the project identifier and project name. The shared REST status overview response will expose the same nested structure so the UI can render labels without an extra lookup while keeping project display data grouped together.

This keeps the change narrowly focused on the client-visible need while producing a cleaner contract than two flat project-related fields on the overview entry.

Alternatives considered:

- Replace `projectId` with a nested project object: rejected for now because it broadens the contract more than the current requirement needs.
- Return only `projectName` without `projectId`: rejected because callers still need the stable project reference.
- Keep `projectId` and `projectName` as separate flat fields: rejected because the nested project object is clearer and scales better if more project display fields are added later.

## Risks / Trade-offs

- [Project snapshot lookups add an extra query step to the overview path] → Use a bulk lookup by distinct `ProjectId` values and keep the query scoped to overview projects only.
- [The overview read model grows beyond pure task state] → Limit enrichment to display-oriented project context that is required by clients.
- [Future UI needs may request more project fields] → Reuse the same snapshot read model so additional additive fields can be introduced without redesigning the lookup path.
- [Current project names are not historical month-end snapshots] → Accept current project metadata for this UI-oriented overview unless a later requirement demands point-in-time naming.

## Migration Plan

This is an additive change with no schema migration.

1. Extend the month-end project snapshot read model and port to support bulk lookup and expose `name`.
2. Enrich status overview entries in the application service using bulk project snapshot reads and a nested project reference value object.
3. Extend the shared overview entry model in the canonical OpenAPI document with a nested project reference schema and regenerate the REST models.
4. Update application tests and REST mapper/resource tests for the enriched overview fields.

Rollback strategy:

- remove the added overview field from the application and REST models
- revert the project snapshot port extension
- restore the previous OpenAPI contract for status overview entries

## Open Questions

None.
