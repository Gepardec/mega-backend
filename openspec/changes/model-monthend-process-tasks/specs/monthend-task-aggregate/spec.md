## ADDED Requirements

### Requirement: Month-end tasks unify all month-end obligations
The system SHALL represent each month-end obligation as a `MonthEndTask` that belongs to one month, one project, and one task type. A month-end task SHALL define its eligible actors and its completion policy within the same aggregate.

#### Scenario: Employee-owned task is modeled as a month-end task
- **WHEN** the system creates an employee time-booking task for an assigned employee
- **THEN** it creates one `MonthEndTask` with that employee as the only eligible actor

#### Scenario: Lead-eligible task is modeled as a month-end task
- **WHEN** the system creates a project lead review task for an employee on a project
- **THEN** it creates one `MonthEndTask` with the fixed set of assigned project leads as eligible actors

### Requirement: Month-end tasks enforce type-specific invariants
The system SHALL enforce task-type-specific invariants inside the `MonthEndTask` aggregate. `PROJECT_LEAD_REVIEW` tasks MUST reference a subject employee, `ABRECHNUNG` tasks MUST NOT reference a subject employee, and employee-owned tasks MUST use a single eligible employee actor.

#### Scenario: Project lead review requires a subject employee
- **WHEN** the system attempts to create a `PROJECT_LEAD_REVIEW` task without a subject employee
- **THEN** the aggregate rejects the task as invalid

#### Scenario: Abrechnung excludes a subject employee
- **WHEN** the system attempts to create an `ABRECHNUNG` task with a subject employee
- **THEN** the aggregate rejects the task as invalid

### Requirement: Month-end tasks preserve generation-time actor eligibility
The system SHALL treat the eligible actor set of a `MonthEndTask` as the snapshot captured at generation time for that month-end run.

#### Scenario: Lead changes after generation do not alter an open task
- **WHEN** project lead assignments change after a month-end task has already been generated
- **THEN** the existing task keeps its original eligible actor set

