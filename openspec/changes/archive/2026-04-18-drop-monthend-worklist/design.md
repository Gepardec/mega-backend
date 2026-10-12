## Context

The month-end worklist was implemented as an early query feature exposing open `MonthEndTask` and open `MonthEndClarification` records enriched with `ProjectRef` and `UserRef`. It predates the status overview, which has since become the canonical read model for the frontend. The worklist endpoints have never been consumed by any frontend code and there are no plans to introduce a worklist-based UI.

The worklist touches:
- Two domain models carrying presentation-enriched data (`MonthEndWorklistItem`, `MonthEndWorklistClarificationItem`)
- Two application services that call outbound snapshot ports to enrich those models
- Two inbound use case ports
- Two REST resource methods and associated mapper methods
- Two OpenAPI path entries and three schema definitions

## Goals / Non-Goals

**Goals:**
- Remove all worklist code, tests, OpenAPI definitions, and specs cleanly with no renames or intermediate states
- Leave the codebase in a consistent state where no dead references remain

**Non-Goals:**
- Changing worklist behaviour (there is none to change)
- Refactoring the status overview or snapshot lookup infrastructure (separate change)
- Deprecation period or versioning — these endpoints have no consumers

## Decisions

**Delete outright, no deprecation markers**
The endpoints are internal (behind Keycloak/OIDC), the frontend has never called them, and there is no external API contract to honour. Adding `@Deprecated` or version headers would create noise without benefit.

**Keep `findOpenEmployeeClarifications` and `findOpenProjectLeadClarifications` repository methods**
These two methods are also used by the status overview's clarification queries. Only the worklist-specific framing in the specs is removed; the methods themselves stay.

**Keep `ResolveMonthEndTaskSnapshotLookupService`**
It is still used by `GetEmployeeMonthEndStatusOverviewService` and `GetProjectLeadMonthEndStatusOverviewService` via `AssembleMonthEndStatusOverviewService`. Removing it is deferred to the follow-on change that refactors the overview to return raw `MonthEndTask` objects.

## Risks / Trade-offs

- **Broken client calls** → No known clients; both REST resources are behind role-based auth. Accept.
- **Accidental reintroduction** → Deleting the spec ensures future contributors have no spec to implement against. Mitigated.

## Migration Plan

No data migration required. No schema changes. Deployment is a standard redeploy — the endpoints simply stop existing.

Rollback: revert the PR. No state is modified at runtime.
