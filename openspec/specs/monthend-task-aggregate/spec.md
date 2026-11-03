# Month-End Task Aggregate

## Purpose

Defines the unified `MonthEndTask` aggregate for month-end obligations in the hexagonal domain, including task identity, subject employee semantics, eligible actors, and aggregate invariants.

## Requirements

### Requirement: Month-end tasks unify all month-end obligations
The system SHALL represent each month-end obligation as a `MonthEndTask` that belongs to one month, one project, and one task type. A month-end task SHALL define its eligible actors, and its task type SHALL determine its completion policy.

#### Scenario: Employee-owned task is modeled as a month-end task
- **WHEN** the system creates an employee time-booking task for an assigned employee
- **THEN** it creates one `MonthEndTask` with that employee as both subject employee and only eligible actor

#### Scenario: Lead-eligible task is modeled as a month-end task
- **WHEN** the system creates a project lead review task for an employee on a project
- **THEN** it creates one `MonthEndTask` with the fixed set of assigned project leads as eligible actors

### Requirement: Month-end tasks enforce type-specific invariants
The system SHALL enforce task-type-specific invariants inside the `MonthEndTask` aggregate. `PROJECT_LEAD_REVIEW` tasks MUST reference a subject employee, `ABRECHNUNG` tasks MUST NOT reference a subject employee, and employee-owned tasks MUST reference the employee they are about while using that same single employee as their eligible actor.

#### Scenario: Project lead review requires a subject employee
- **WHEN** the system attempts to create a `PROJECT_LEAD_REVIEW` task without a subject employee
- **THEN** the aggregate rejects the task as invalid

#### Scenario: Abrechnung excludes a subject employee
- **WHEN** the system attempts to create an `ABRECHNUNG` task with a subject employee
- **THEN** the aggregate rejects the task as invalid

#### Scenario: Employee-owned task requires its subject employee
- **WHEN** the system attempts to create an employee-owned task without a subject employee
- **THEN** the aggregate rejects the task as invalid

### Requirement: Month-end tasks preserve generation-time actor eligibility
The system SHALL treat the eligible actor set of a `MonthEndTask` as the snapshot captured at generation time for that month-end run.

#### Scenario: Lead changes after generation do not alter an open task
- **WHEN** project lead assignments change after a month-end task has already been generated
- **THEN** the existing task keeps its original eligible actor set

### Requirement: Each month-end task type is the duty of one capacity
Every month-end task type SHALL be the duty of exactly one capacity. `EMPLOYEE_TIME_CHECK` SHALL be a duty of the `EMPLOYEE` capacity. `LEISTUNGSNACHWEIS`, `PROJECT_LEAD_REVIEW`, and `ABRECHNUNG` SHALL be duties of the `CONTROLLING` capacity. The duty SHALL only decide where a task is offered as completable. It SHALL NOT change who may complete the task.

#### Scenario: Time check is an employee duty
- **WHEN** the duty of an `EMPLOYEE_TIME_CHECK` task is determined
- **THEN** it is the `EMPLOYEE` capacity

#### Scenario: Lead-eligible task types are controlling duties
- **WHEN** the duty of a `LEISTUNGSNACHWEIS`, `PROJECT_LEAD_REVIEW`, or `ABRECHNUNG` task is determined
- **THEN** it is the `CONTROLLING` capacity

#### Scenario: Duty does not restrict completion eligibility
- **WHEN** an actor in a task's eligible actor set completes that task
- **THEN** the completion succeeds regardless of the task type's duty
