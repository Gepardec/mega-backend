## MODIFIED Requirements

### Requirement: Month-end clarifications model user-created follow-up subtasks
The system SHALL represent each month-end clarification as a `MonthEndClarification` scoped to one month and one project. A clarification SHALL store its creator (`createdBy`), an optional subject employee (`subjectEmployeeId`), open or done status, clarification text, optional resolution note, eligible project lead snapshot, `createdAt`, optional `resolvedAt`, and `lastModifiedAt`. The `creatorSide` field is removed. When `subjectEmployeeId` is absent the clarification is project-level and the creator MUST be an eligible project lead.

Clarification display enrichment — resolving user identifiers to display references and evaluating actor-specific capability flags — is NOT a domain or application concern. The `MonthEndStatusOverview` aggregate SHALL hold `List<MonthEndClarification>` and SHALL NOT hold a pre-enriched presentation model. No domain or application class SHALL build or hold a `MonthEndOverviewClarificationItem` or equivalent presentation-layer view of a clarification.
