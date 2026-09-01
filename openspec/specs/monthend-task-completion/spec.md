# Month-End Task Completion

## Purpose

Defines unified completion behavior for month-end obligations, including actor eligibility checks, completion state transitions, and completer tracking.

## Requirements

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

### Requirement: Ineligible actors cannot complete a month-end task
The system MUST reject completion attempts from actors that are not in the task's eligible actor set.

#### Scenario: Another employee cannot complete a task
- **WHEN** a different employee attempts to complete an employee-owned month-end task
- **THEN** the system rejects the completion attempt

#### Scenario: Non-eligible lead cannot complete a project task
- **WHEN** a user who is not in the eligible lead set attempts to complete a project-owned month-end task
- **THEN** the system rejects the completion attempt

### Requirement: Completion records who satisfied the obligation
The system SHALL retain the actor who first completed a month-end task. When completed by the system actor, `completedBy` SHALL be `SystemActor.USER_ID`.

#### Scenario: First completer is stored for a lead-eligible task
- **WHEN** one eligible project lead completes an open lead-eligible month-end task
- **THEN** the task records that lead as the completing actor

#### Scenario: System completion records SystemActor.USER_ID as completer
- **WHEN** `task.completeBySystem()` completes an open task
- **THEN** `completedBy` equals `SystemActor.USER_ID`

#### Scenario: Repeated completion remains idempotent
- **WHEN** a completed month-end task is completed again by another eligible actor
- **THEN** the task remains `DONE` and keeps the originally recorded completing actor

### Requirement: Eligible actors can complete a scoped set of same-type tasks in one operation
The system SHALL support completing, in a single operation, all month-end tasks that match a given month, project, and task type. The operation SHALL complete only tasks that are currently open and whose eligible actor set includes the acting actor; matching tasks that are already done or for which the actor is not eligible SHALL be skipped without error, so partial success is a normal outcome. Each completed task SHALL follow the same per-task completion rules and completer tracking used for single-task completion. The operation SHALL be atomic with respect to unexpected failures: if an unexpected error occurs while completing the set, no task in the set is completed.

#### Scenario: All open eligible tasks in scope are completed
- **WHEN** an eligible project lead bulk-completes all tasks of one lead-eligible type for a project in a month
- **THEN** every such open task whose eligible actor set includes that lead becomes `DONE`
- **THEN** each completed task records that lead as the completing actor
- **THEN** the operation returns the set of tasks it transitioned to `DONE`

#### Scenario: Already-done tasks in scope are skipped
- **WHEN** the requested scope contains tasks that are already `DONE`
- **THEN** those tasks remain `DONE` and keep their original completing actor
- **THEN** those tasks are not included in the set of newly completed tasks

#### Scenario: Re-running an already-completed scope completes nothing
- **WHEN** the same scope is bulk-completed again after all of its tasks are already `DONE`
- **THEN** no task is completed
- **THEN** the returned set of newly completed tasks is empty

#### Scenario: Tasks the actor is not eligible for are skipped, not failed
- **WHEN** the requested scope contains a task whose eligible actor set does not include the acting actor
- **THEN** that task is left unchanged
- **THEN** the operation does not fail and continues completing the remaining eligible tasks

#### Scenario: Bulk completion is atomic on unexpected failure
- **WHEN** an unexpected error occurs while completing the matching tasks
- **THEN** none of the tasks in the scope are left completed

### Requirement: An employee can complete their own open time-check tasks in one operation
The system SHALL support completing, in a single operation, all `EMPLOYEE_TIME_CHECK` tasks for a given month whose subject employee is the acting actor, optionally narrowed to a single project. The operation SHALL complete only tasks that are currently open and whose eligible actor set includes the acting actor; matching tasks that are already done or for which the actor is not eligible SHALL be skipped without error, so partial success is a normal outcome. The operation SHALL NOT widen eligibility: an actor SHALL only ever complete time-check tasks for which they are the subject employee. Each completed task SHALL follow the same per-task completion rules and completer tracking used for single-task completion. The operation SHALL be atomic with respect to unexpected failures: if an unexpected error occurs while completing the set, no task in the set is completed.

#### Scenario: All open time-check tasks of the actor in the month are completed
- **WHEN** an employee bulk-completes their time-check tasks for a month without naming a project
- **THEN** every open `EMPLOYEE_TIME_CHECK` task in that month whose subject is that employee becomes `DONE`
- **THEN** each completed task records that employee as the completing actor
- **THEN** the operation returns the set of tasks it transitioned to `DONE`

#### Scenario: Scope is narrowed to one project when a project is named
- **WHEN** an employee bulk-completes their time-check tasks for a month and names a project
- **THEN** only their open time-check tasks for that project and month become `DONE`
- **THEN** their open time-check tasks on other projects are left unchanged

#### Scenario: Another employee's time-check tasks are never in scope
- **WHEN** an employee bulk-completes their time-check tasks for a month
- **THEN** time-check tasks whose subject is a different employee are left unchanged, including on projects the acting employee is assigned to

#### Scenario: Already-done tasks in scope are skipped
- **WHEN** the requested scope contains time-check tasks that are already `DONE`
- **THEN** those tasks remain `DONE` and keep their original completing actor
- **THEN** those tasks are not included in the set of newly completed tasks

#### Scenario: Empty scope completes nothing
- **WHEN** the acting employee has no open time-check tasks in the requested scope
- **THEN** no task is completed
- **THEN** the returned set of newly completed tasks is empty

#### Scenario: Bulk completion is atomic on unexpected failure
- **WHEN** an unexpected error occurs while completing the matching tasks
- **THEN** none of the tasks in the scope are left completed
