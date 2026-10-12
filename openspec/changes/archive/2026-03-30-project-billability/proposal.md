## Why

The hexagonal `Project` aggregate currently has no concept of billability, but the upcoming month-end check generation needs to distinguish customer (billable) projects from internal ones in order to generate `LEISTUNGSNACHWEIS` and `ABRECHNUNG` checks only where applicable.

## What Changes

- Add `billable: boolean` field to the `Project` aggregate
- Extend `ZepProjectProfile` to carry the billability flag sourced from ZEP
- Update `Project.create(...)`, `Project.reconstitute(...)`, and `Project.syncFromZep(...)` to include `billable`
- Update `ZepProjectAdapter` to map the billability from the ZEP project response
- Update the `ProjectMapper` MapStruct mapper to include the new field
- Add a `billable` column to the `project` table via a Liquibase migration
- Update `ProjectEntity` and `ProjectRepositoryAdapter` accordingly

## Capabilities

### New Capabilities

_(none — this change extends an existing capability)_

### Modified Capabilities

- `project-aggregate`: Adds `billable` as a required field on the `Project` aggregate and its supporting value object `ZepProjectProfile`; updates all factory methods and the sync method to carry the flag.
- `project-sync`: ZEP-to-domain mapping must now extract and propagate the billability flag from the ZEP project payload.

## Impact

- `com.gepardec.mega.hexagon.project.domain.model.Project` — new field
- `com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile` — new field
- `com.gepardec.mega.hexagon.project.adapter.outbound.ZepProjectAdapter` — mapping change
- `com.gepardec.mega.hexagon.project.adapter.outbound.ProjectMapper` — MapStruct update
- `com.gepardec.mega.hexagon.project.adapter.outbound.ProjectEntity` — new column
- `com.gepardec.mega.hexagon.project.adapter.outbound.ProjectRepositoryAdapter` — minor update
- `src/main/resources/db/` — new Liquibase changelog entry
- No REST API changes; no breaking changes to consumers
