# Spec Delta

## MODIFIED Requirements

### Requirement: Month-end tasks unify all month-end obligations
The system SHALL represent each month-end obligation as a `MonthEndTask` that belongs to one month, one project, and one task type. A month-end task SHALL define its eligible actors, and its task type SHALL determine its completion policy.

#### Scenario: Employee-owned task is modeled as a month-end task
- **WHEN** the system creates an employee time-booking task for an assigned employee
- **THEN** it creates one `MonthEndTask` with that employee as both subject employee and only eligible actor

#### Scenario: Lead-eligible task is modeled as a month-end task
- **WHEN** the system creates a project lead review task for an employee on a project
- **THEN** it creates one `MonthEndTask` with the fixed set of assigned project leads as eligible actors

#### Scenario: Coordinator-eligible task is modeled as a month-end task
- **WHEN** the system creates a Leistungsnachweis task for an employee on a project
- **THEN** it creates one `MonthEndTask` with the fixed set of active project coordinators as eligible actors
- **THEN** any one of those coordinators can complete it

### Requirement: Month-end tasks preserve generation-time actor eligibility
The system SHALL treat the eligible actor set of a `MonthEndTask` as the snapshot captured at generation time for that month-end run.

#### Scenario: Lead changes after generation do not alter an open task
- **WHEN** project lead assignments change after a month-end task has already been generated
- **THEN** the existing task keeps its original eligible actor set

#### Scenario: Coordinator changes after generation do not alter an open task
- **WHEN** the set of users holding the project coordinator role changes after a `LEISTUNGSNACHWEIS` task has already been generated
- **THEN** the existing task keeps its original eligible actor set
