# Month-End Task Worklist

## Purpose

Defines the employee and project-lead month-end worklists as query views over open `MonthEndTask` aggregates, including the business context needed to act on each task.

## Requirements

### Requirement: Employee and lead worklists are derived from month-end tasks
The system SHALL provide actor-specific month-end worklists by querying `MonthEndTask` aggregates for a given actor and month.

#### Scenario: Employee worklist shows the employee's open tasks
- **WHEN** an employee requests their month-end worklist for a month
- **THEN** the system returns the open month-end tasks where that employee is an eligible actor

#### Scenario: Lead worklist shows shared project tasks for eligible leads
- **WHEN** a project lead requests their month-end worklist for a month
- **THEN** the system returns the open month-end tasks where that lead is in the eligible actor set

### Requirement: Worklists expose the business context needed to act on a task
The system SHALL include the task type, project reference, and subject employee reference when present in worklist results.

#### Scenario: Lead sees which employee must be reviewed
- **WHEN** a lead receives a `PROJECT_LEAD_REVIEW` task in their worklist
- **THEN** the task includes the reviewed employee reference

#### Scenario: Employee sees which project requires action
- **WHEN** an employee receives an employee-owned month-end task in their worklist
- **THEN** the task includes the associated project reference

### Requirement: Completed shared tasks disappear from all eligible lead worklists
The system SHALL remove a lead-eligible task from every eligible lead's open worklist once one lead has completed it.

#### Scenario: Shared task disappears for all leads after completion
- **WHEN** one eligible lead completes an open lead-eligible month-end task
- **THEN** that task no longer appears in the open worklist of any eligible lead
