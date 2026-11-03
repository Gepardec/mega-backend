# Spec Delta

## MODIFIED Requirements

### Requirement: Eligible actors can complete month-end tasks through one completion flow
The system SHALL complete all month-end obligations through the `MonthEndTask` aggregate, regardless of whether the task is employee-owned, lead-eligible, or coordinator-eligible. The system actor (`SystemActor.USER_ID`) SHALL also be able to complete any task via `completeBySystem()` without being present in `eligibleActorIds`.

#### Scenario: Employee completes an employee-owned task
- **WHEN** the assigned employee completes an open employee-owned month-end task
- **THEN** the task status becomes `DONE`

#### Scenario: Lead completes a shared project task
- **WHEN** one eligible project lead completes an open `PROJECT_LEAD_REVIEW` or `ABRECHNUNG` task
- **THEN** the task status becomes `DONE`

#### Scenario: Coordinator completes a Leistungsnachweis task
- **WHEN** one eligible project coordinator completes an open `LEISTUNGSNACHWEIS` task
- **THEN** the task status becomes `DONE`
- **THEN** the task records that coordinator as the completing actor

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

#### Scenario: Project lead cannot complete a coordinator-eligible Leistungsnachweis task
- **WHEN** a project lead who does not hold the project coordinator role attempts to complete a `LEISTUNGSNACHWEIS` task of a project they lead, generated with project coordinators as eligible actors
- **THEN** the system rejects the completion attempt

### Requirement: Project leads can bulk complete one task type for a project
The system SHALL complete, in a single operation, the open month-end tasks of one task type for one project and month on which the acting actor is an eligible actor, and SHALL return exactly the tasks it transitioned to `DONE`. Project leads use it for `PROJECT_LEAD_REVIEW`; project coordinators use it for `LEISTUNGSNACHWEIS`.

#### Scenario: Project lead completes all open tasks of one type for a project
- **WHEN** a project lead bulk-completes `PROJECT_LEAD_REVIEW` tasks for a project they lead in a month
- **THEN** every open `PROJECT_LEAD_REVIEW` task of that project and month whose eligible actor set includes that lead becomes `DONE`
- **THEN** each completed task records that lead as the completing actor
- **THEN** the operation returns the set of tasks it transitioned to `DONE`

#### Scenario: Coordinator completes all open Leistungsnachweis tasks for a project
- **WHEN** a project coordinator bulk-completes `LEISTUNGSNACHWEIS` tasks for a project in a month
- **THEN** every open `LEISTUNGSNACHWEIS` task of that project and month whose eligible actor set includes that coordinator becomes `DONE`
- **THEN** each completed task records that coordinator as the completing actor
- **THEN** the operation returns the set of tasks it transitioned to `DONE`

#### Scenario: Lead with no tasks of the requested type receives an empty result
- **WHEN** a project lead bulk-completes `PROJECT_LEAD_REVIEW` tasks for a project they lead, is eligible on its `PROJECT_LEAD_REVIEW` tasks in that month, and all of those tasks are already `DONE`
- **THEN** no task is completed
- **THEN** the returned set of newly completed tasks is empty

### Requirement: Project-lead bulk completion is authorized by generation-time task eligibility
The project-lead bulk completion operation SHALL reject the acting actor as not authorized unless they are an eligible actor on at least one task **of the requested type** of that project in that month, as recorded at generation time. Eligibility on tasks of another type SHALL NOT authorize the request. Changes to project leads or project coordinators after generation SHALL NOT affect this.

#### Scenario: Lead removed from the project after generation can still bulk complete
- **WHEN** a project lead was an eligible actor on the project's `PROJECT_LEAD_REVIEW` tasks at generation time and has since been removed as lead of the project
- **THEN** a project-lead bulk completion of `PROJECT_LEAD_REVIEW` by that lead for that project and month completes the open tasks of that type on which they are eligible

#### Scenario: Lead added to the project after generation is not authorized
- **WHEN** an actor became lead of a project after that month's tasks were generated and is an eligible actor on none of the project's tasks for that month
- **THEN** a project-lead bulk completion by that actor for that project and month is rejected as not authorized
- **THEN** no task is completed

#### Scenario: Actor who does not lead the project is not authorized
- **WHEN** an actor who is not an eligible actor on any task of the requested type of a project in a month requests a project-lead bulk completion for that project, type, and month
- **THEN** the operation is rejected as not authorized

#### Scenario: Lead requesting Leistungsnachweis is not authorized
- **WHEN** a project lead who is not a project coordinator requests a bulk completion of `LEISTUNGSNACHWEIS` for a project they lead, whose `LEISTUNGSNACHWEIS` tasks were generated with project coordinators as eligible actors
- **THEN** the operation is rejected as not authorized
- **THEN** no task is completed

#### Scenario: Coordinator requesting lead review on a project they do not lead is not authorized
- **WHEN** a project coordinator who is eligible on a project's `LEISTUNGSNACHWEIS` tasks but not on its `PROJECT_LEAD_REVIEW` tasks requests a bulk completion of `PROJECT_LEAD_REVIEW` for that project and month
- **THEN** the operation is rejected as not authorized

#### Scenario: Request for a type with no tasks in the project is not authorized
- **WHEN** an actor requests a bulk completion of `LEISTUNGSNACHWEIS` for a project and month that has no `LEISTUNGSNACHWEIS` tasks
- **THEN** the operation is rejected as not authorized
