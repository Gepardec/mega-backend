## MODIFIED Requirements

### Requirement: Status overview is derived from relevant month-end tasks for an actor and month
The system SHALL provide a month-end status overview for a requesting actor and month by querying all `MonthEndTask` obligations that are visible to that actor, including both `OPEN` and `DONE` tasks. A task is visible to an actor if the actor is in `eligibleActorIds` OR is the `subjectEmployeeId` of that task.

#### Scenario: Employee overview includes open and completed employee-owned tasks
- **WHEN** an employee requests their month-end status overview for a month in which some of their employee-owned month-end tasks are open and others are done
- **THEN** the system returns both the open and completed employee-owned tasks in the overview

#### Scenario: Lead overview includes completed shared tasks
- **WHEN** a project lead requests their month-end status overview for a month after an eligible lead has completed a shared lead-eligible month-end task
- **THEN** the system still returns that completed shared task in the lead's overview

#### Scenario: Employee overview includes PROJECT_LEAD_REVIEW tasks about that employee
- **WHEN** an employee requests their month-end status overview for a month that contains `PROJECT_LEAD_REVIEW` tasks where that employee is the subject
- **THEN** the system returns those `PROJECT_LEAD_REVIEW` tasks in the employee's overview

#### Scenario: Employee sees at most one entry per task even when subject and eligible
- **WHEN** an employee requests their month-end status overview and has tasks where they are both the subject employee and in the eligible actor set
- **THEN** each such task appears exactly once in the overview

## ADDED Requirements

### Requirement: Status overview entries expose whether the actor can complete the task
The system SHALL include a `canComplete` flag on each overview entry indicating whether the requesting actor is eligible to complete that task. `canComplete` SHALL be `true` if and only if the actor is in `eligibleActorIds` for that task.

#### Scenario: Eligible actor sees canComplete true for their own task
- **WHEN** an employee requests their status overview and an entry corresponds to a task where that employee is in `eligibleActorIds`
- **THEN** that overview entry has `canComplete` set to `true`

#### Scenario: Subject-only actor sees canComplete false for a PROJECT_LEAD_REVIEW task
- **WHEN** an employee requests their status overview and an entry corresponds to a `PROJECT_LEAD_REVIEW` task where they are the subject employee but not in `eligibleActorIds`
- **THEN** that overview entry has `canComplete` set to `false`

#### Scenario: Eligible always wins when actor is both subject and eligible
- **WHEN** an actor is both the subject employee and in `eligibleActorIds` for a task
- **THEN** that overview entry has `canComplete` set to `true`
