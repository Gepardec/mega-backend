## ADDED Requirements

### Requirement: Status overview includes all clarifications related to the actor's scope for that month
The system SHALL include all `MonthEndClarification` records related to the actor's visible scope in the `MonthEndStatusOverview`, regardless of clarification status (`OPEN` or `DONE`):

- **Employee overview**: all clarifications where the employee is the `subjectEmployeeId` for that month.
- **Lead overview**: all clarifications for projects the lead leads for that month (same project scope as the lead's task query).

Each clarification in the overview SHALL be represented as a `MonthEndOverviewClarificationItem` containing: clarification identity, project, subject employee, creator, creator side, status, text, `canResolve`, and — when `DONE` — resolution note, resolver, and resolved timestamp.

`canResolve` SHALL be `true` if and only if the requesting actor can resolve that clarification according to the clarification's resolution rules: eligible project leads may resolve employee-created clarifications; the subject employee may resolve lead-created clarifications. `canResolve` SHALL be `false` for `DONE` clarifications.

#### Scenario: Employee overview includes their open clarifications
- **WHEN** an employee requests their month-end status overview for a month in which they have open clarifications as subject employee
- **THEN** the overview includes those clarifications with status `OPEN`

#### Scenario: Employee overview includes resolved clarifications
- **WHEN** an employee requests their month-end status overview for a month in which some of their clarifications have been resolved
- **THEN** the overview includes those clarifications with status `DONE` and populated resolution fields

#### Scenario: canResolve reflects whether the actor can resolve each clarification
- **WHEN** the employee requests their overview and has an open lead-created clarification scoped to them
- **THEN** that clarification item has `canResolve` set to `true`
- **WHEN** the employee requests their overview and has an open employee-created clarification
- **THEN** that clarification item has `canResolve` set to `false`

#### Scenario: canResolve is false for done clarifications
- **WHEN** the overview contains a `DONE` clarification
- **THEN** that clarification item has `canResolve` set to `false`

#### Scenario: Lead overview includes all clarifications for their projects
- **WHEN** a project lead requests their month-end status overview for a month
- **THEN** the overview includes all clarifications (OPEN and DONE) for all projects the lead leads that month

#### Scenario: Lead can resolve employee-created clarifications they are eligible for
- **WHEN** the lead overview contains an open employee-created clarification for a project the lead leads
- **THEN** that clarification item has `canResolve` set to `true`

#### Scenario: Overview clarification items include resolution details for done clarifications
- **WHEN** a `DONE` clarification appears in the overview
- **THEN** the clarification item includes the resolver identity, resolution timestamp, and optional resolution note
