## MODIFIED Requirements

### Requirement: Eligible actors can complete month-end tasks through one completion flow
The system SHALL complete all month-end obligations through the `MonthEndTask` aggregate, regardless of whether the task is employee-owned or lead-eligible. The system actor (`SystemActor.USER_ID`) SHALL also be able to complete any task via `completeBySystem()` without being present in `eligibleActorIds`.

#### Scenario: Employee completes an employee-owned task
- **WHEN** the assigned employee completes an open employee-owned month-end task
- **THEN** the task status becomes `DONE`

#### Scenario: Lead completes a shared project task
- **WHEN** one eligible project lead completes an open `PROJECT_LEAD_REVIEW`, `LEISTUNGSNACHWEIS`, or `ABRECHNUNG` task
- **THEN** the task status becomes `DONE`

#### Scenario: System actor completes an employee-owned task on behalf of an absent employee
- **WHEN** `task.completeBySystem()` is called on an open employee-owned task
- **THEN** the task status becomes `DONE`
- **THEN** `completedBy` is set to `SystemActor.USER_ID`
