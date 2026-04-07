# Month-End Task Worklist

## Purpose

Defines the employee and project-lead month-end worklists as query views over open `MonthEndTask` and visible open `MonthEndClarification` aggregates, including the business context needed to act on each item.

## Requirements

### Requirement: Employee and lead worklists are derived from month-end tasks and clarifications
The system SHALL provide actor-specific month-end worklists by querying generated open `MonthEndTask` aggregates and visible open `MonthEndClarification` aggregates for a given actor and month.

#### Scenario: Employee worklist shows the employee's open tasks and clarifications
- **WHEN** an employee requests their month-end worklist for a month
- **THEN** the system returns the open month-end tasks where that employee is an eligible actor
- **THEN** the system returns the open clarifications where that employee is the subject employee

#### Scenario: Lead worklist shows shared project tasks and visible clarifications
- **WHEN** a project lead requests their month-end worklist for a month
- **THEN** the system returns the open month-end tasks where that lead is in the eligible actor set
- **THEN** the system returns the open clarifications whose eligible lead snapshot contains that lead

### Requirement: Worklists expose the business context needed to act on a task or clarification
The system SHALL include the task type, project reference (id and name), and subject employee reference (id and full name) when present for generated tasks, and SHALL include the project reference, subject employee reference, clarification text, creator metadata, and clarification status metadata for visible clarifications.

#### Scenario: Lead sees which employee must be reviewed
- **WHEN** a lead receives a `PROJECT_LEAD_REVIEW` task in their worklist
- **THEN** the task includes the reviewed employee reference with both the employee id and full name

#### Scenario: Employee sees which project requires action
- **WHEN** an employee receives an employee-owned month-end task in their worklist
- **THEN** the task includes the associated project reference with both the project id and name

#### Scenario: Lead sees which employee a visible clarification belongs to
- **WHEN** a lead receives a clarification in their worklist
- **THEN** the clarification includes the associated project reference with both the project id and name
- **THEN** the clarification includes the associated subject employee reference with both the employee id and full name

#### Scenario: Employee sees clarification details needed to act
- **WHEN** an employee receives a clarification in their worklist
- **THEN** the clarification includes its text and creator metadata
- **THEN** the clarification exposes whether it is still open or already done

### Requirement: Completed shared tasks disappear from all eligible lead worklists
The system SHALL remove a lead-eligible task from every eligible lead's open worklist once one lead has completed it.

#### Scenario: Shared task disappears for all leads after completion
- **WHEN** one eligible lead completes an open lead-eligible month-end task
- **THEN** that task no longer appears in the open worklist of any eligible lead

### Requirement: Resolved clarifications disappear from open worklists
The system SHALL remove a clarification from actor-specific open worklists once it has been resolved.

#### Scenario: Employee-created clarification disappears from all visible worklists after lead resolution
- **WHEN** an eligible project lead resolves an open employee-created clarification
- **THEN** that clarification no longer appears in the employee's open worklist
- **THEN** that clarification no longer appears in the open worklist of any eligible project lead

#### Scenario: Lead-created clarification disappears from all visible worklists after employee resolution
- **WHEN** the subject employee resolves an open lead-created clarification
- **THEN** that clarification no longer appears in the employee's open worklist
- **THEN** that clarification no longer appears in the open worklist of any eligible project lead
