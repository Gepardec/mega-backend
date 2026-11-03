## ADDED Requirements

### Requirement: Clarification overview query is scoped by capacity
The system SHALL provide a repository query that returns all clarifications in a capacity scope for a given month, regardless of clarification status:

- **Employee capacity scope**: all `MonthEndClarification` records where the actor is the `subjectEmployeeId` for that month, including both `OPEN` and `DONE`.
- **Controlling capacity scope**: all `MonthEndClarification` records for projects the actor leads for that month, including both `OPEN` and `DONE`.

#### Scenario: Employee capacity scope returns open and done clarifications
- **WHEN** clarifications are queried in the employee capacity scope for an actor who has both open and done clarifications as subject employee in that month
- **THEN** both open and done clarifications are returned

#### Scenario: Employee capacity scope excludes clarifications from other months
- **WHEN** clarifications are queried in the employee capacity scope for a given month
- **THEN** clarifications from other months are not returned

#### Scenario: Controlling capacity scope returns clarifications for all led projects
- **WHEN** clarifications are queried in the controlling capacity scope for a lead who leads multiple projects
- **THEN** clarifications for all those projects are returned, regardless of status

#### Scenario: Controlling capacity scope excludes clarifications from projects the lead does not lead
- **WHEN** clarifications are queried in the controlling capacity scope
- **THEN** clarifications from projects where the lead has no eligible-actor role are not returned

## REMOVED Requirements

### Requirement: Clarification repository supports full-status queries for actor-scoped month-end overviews
**Reason**: The two role-named queries are replaced by one query scoped by capacity, matching the capacity-parameterized status overview.
**Migration**: Query overview clarifications by capacity scope: the employee capacity scope covers the former employee query, and the controlling capacity scope covers the former project-lead query. The behavior is unchanged.
