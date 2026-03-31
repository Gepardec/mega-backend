## ADDED Requirements

### Requirement: Eligible actors can complete month-end tasks through one completion flow
The system SHALL complete all month-end obligations through the `MonthEndTask` aggregate, regardless of whether the task is employee-owned or lead-eligible.

#### Scenario: Employee completes an employee-owned task
- **WHEN** the assigned employee completes an open employee-owned month-end task
- **THEN** the task status becomes `DONE`

#### Scenario: Lead completes a shared project task
- **WHEN** one eligible project lead completes an open `PROJECT_LEAD_REVIEW` or `ABRECHNUNG` task
- **THEN** the task status becomes `DONE`

### Requirement: Ineligible actors cannot complete a month-end task
The system MUST reject completion attempts from actors that are not in the task's eligible actor set.

#### Scenario: Another employee cannot complete a task
- **WHEN** a different employee attempts to complete an employee-owned month-end task
- **THEN** the system rejects the completion attempt

#### Scenario: Non-eligible lead cannot complete a project task
- **WHEN** a user who is not in the eligible lead set attempts to complete a project-owned month-end task
- **THEN** the system rejects the completion attempt

### Requirement: Completion records who satisfied the obligation
The system SHALL retain the actor who first completed a month-end task.

#### Scenario: First completer is stored for a lead-eligible task
- **WHEN** one eligible project lead completes an open lead-eligible month-end task
- **THEN** the task records that lead as the completing actor

#### Scenario: Repeated completion remains idempotent
- **WHEN** a completed month-end task is completed again by another eligible actor
- **THEN** the task remains `DONE` and keeps the originally recorded completing actor

