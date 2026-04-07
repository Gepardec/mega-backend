## Context

The month-end domain currently has two item types returned from worklist and status overview queries:

- `MonthEndStatusOverviewItem` — already enriched: carries `MonthEndStatusOverviewProject` (id + name) and `MonthEndStatusOverviewSubjectEmployee` (id + fullName) instead of raw IDs.
- `MonthEndWorklistItem` — not enriched: still carries raw `ProjectId` and `UserId`, making those fields useless to callers without additional lookups.

There are also two single-purpose projection records that are conceptually identical but scoped to the status overview: `MonthEndStatusOverviewProject` and `MonthEndStatusOverviewSubjectEmployee`. These are reusable as-is if renamed to remove the `StatusOverview` coupling.

## Goals / Non-Goals

**Goals:**
- Rename `MonthEndStatusOverviewProject` → `MonthEndProject` and `MonthEndStatusOverviewSubjectEmployee` → `MonthEndEmployee` so these projections are context-neutral and reusable.
- Replace raw `ProjectId projectId` and `UserId subjectEmployeeId` in `MonthEndWorklistItem` with `MonthEndProject project` and `MonthEndEmployee subjectEmployee`.
- Update all references (mappers, factories, constructors, tests) to use the new names.

**Non-Goals:**
- Adding new fields to the projections beyond what already exists.
- Changing the REST API shape or introducing new endpoints.
- Touching the `MonthEndStatusOverviewItem` structure beyond the type rename.

## Decisions

### Rename rather than introduce new types
`MonthEndStatusOverviewProject` and `MonthEndStatusOverviewSubjectEmployee` already contain exactly the right fields. Introducing new types would be duplication. The `StatusOverview` infix is an artifact of where they were first created, not an intrinsic property of what they represent.

**Chosen names:** `MonthEndProject`, `MonthEndEmployee`
- `MonthEndProject`: short, reads naturally as "a project reference within the month-end context"
- `MonthEndEmployee`: symmetric, reads naturally as "an employee reference within the month-end context"
- Prefix `MonthEnd` is consistent with all other model types in this package and avoids clashing with generic `Project`/`Employee` domain concepts that may exist elsewhere.

**Alternative considered:** `MonthEndProjectRef` / `MonthEndEmployeeRef` — rejected as overly verbose; the record is already a projection/reference by nature.

## Risks / Trade-offs

- **Rename blast radius is small** — `MonthEndStatusOverviewProject` and `MonthEndStatusOverviewSubjectEmployee` are internal domain model types, not part of a public API contract. All usages are within the `hexagon` package.
- **Mapper enrichment prerequisite** — for `MonthEndWorklistItem` to carry `MonthEndProject` and `MonthEndEmployee`, the mapper that assembles worklist items must have access to project name and employee full name at assembly time. Verify that the data is already available in the query result before building the mapper; if not, a join or secondary lookup will be needed.
