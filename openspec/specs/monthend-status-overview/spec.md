# Month-End Status Overview

## Purpose

Defines a status-oriented month-end overview for matrix or dashboard-style views that need visibility into both open and completed month-end obligations.

## Requirements

### Requirement: Status overview is derived from relevant month-end tasks for an actor and month
The system SHALL provide capacity-scoped month-end status overviews, including both `OPEN` and `DONE` tasks:

- **Employee capacity**: returns all `MonthEndTask` records where the actor is the `subjectEmployeeId`. This covers their own `EMPLOYEE_TIME_CHECK` tasks (where they are also the eligible actor) and `LEISTUNGSNACHWEIS` and `PROJECT_LEAD_REVIEW` tasks about them.
- **Controlling capacity**: returns all `MonthEndTask` records for projects the actor controls. An actor controls a project for a given month if they are in `eligibleActorIds` of any task of that project and month whose type is a duty of the controlling capacity (`LEISTUNGSNACHWEIS`, `PROJECT_LEAD_REVIEW`, or `ABRECHNUNG`). Project leads therefore control the projects they lead, and project coordinators control every project with `LEISTUNGSNACHWEIS` tasks they are eligible on. This includes employee-owned tasks on those projects and shared tasks the actor is not eligible on (visible but not actionable) as well as tasks the actor is eligible on (actionable).

The use case SHALL return a `MonthEndStatusOverview` containing raw `MonthEndTask` domain objects in `entries`. No application service SHALL pre-enrich tasks with display references (`ProjectRef`, `UserRef`) or compute actor-specific flags. Task display enrichment — resolving project and user identifiers to display references and evaluating `canComplete` — is NOT a domain or application concern and SHALL be performed exclusively by the REST adapter.

`canComplete` SHALL be `true` if and only if the actor is in `eligibleActorIds` for that task and the task type is a duty of the requested capacity.

#### Scenario: Employee overview includes open and completed employee-owned tasks
- **WHEN** an actor requests their month-end status overview in the employee capacity for a month in which some of their employee-owned month-end tasks are open and others are done
- **THEN** the system returns both the open and completed employee-owned tasks in the overview

#### Scenario: Employee overview includes PROJECT_LEAD_REVIEW tasks about that employee
- **WHEN** an actor requests their month-end status overview in the employee capacity for a month that contains `PROJECT_LEAD_REVIEW` tasks where that actor is the subject
- **THEN** the system returns those `PROJECT_LEAD_REVIEW` tasks in the overview

#### Scenario: Employee overview includes LEISTUNGSNACHWEIS tasks about that employee
- **WHEN** an actor requests their month-end status overview in the employee capacity for a month that contains `LEISTUNGSNACHWEIS` tasks where that actor is the subject
- **THEN** the system returns those `LEISTUNGSNACHWEIS` tasks in the overview

#### Scenario: Employee overview does not include ABRECHNUNG tasks
- **WHEN** an actor requests their month-end status overview in the employee capacity
- **THEN** the system does not return `ABRECHNUNG` tasks, as those have no subject employee

#### Scenario: Employee sees at most one entry per task even when subject and eligible
- **WHEN** an actor requests their month-end status overview in the employee capacity and has tasks where they are both the subject employee and in the eligible actor set
- **THEN** each such task appears exactly once in the overview

#### Scenario: Lead overview includes all task types for their projects
- **WHEN** a project lead requests their month-end status overview in the controlling capacity for a month
- **THEN** the system returns `EMPLOYEE_TIME_CHECK`, `LEISTUNGSNACHWEIS`, `PROJECT_LEAD_REVIEW`, and `ABRECHNUNG` tasks for all projects the lead leads

#### Scenario: Lead overview does not include tasks from projects they do not lead
- **WHEN** a project lead who is not a project coordinator requests their month-end status overview in the controlling capacity for a month
- **THEN** the system does not return tasks from projects where the lead has no eligible-actor role

#### Scenario: Lead sees EMPLOYEE_TIME_CHECK tasks as read-only
- **WHEN** the overview in the controlling capacity contains an `EMPLOYEE_TIME_CHECK` task
- **THEN** that overview entry has `canComplete` set to `false`

#### Scenario: Lead sees LEISTUNGSNACHWEIS tasks as actionable
- **WHEN** the overview in the controlling capacity contains a `LEISTUNGSNACHWEIS` task generated before project coordinators took over Leistungsnachweis, with the lead as an eligible actor
- **THEN** that overview entry has `canComplete` set to `true`

#### Scenario: Lead sees coordinator-eligible LEISTUNGSNACHWEIS tasks as read-only
- **WHEN** the overview in the controlling capacity of a project lead who is not a project coordinator contains a `LEISTUNGSNACHWEIS` task generated with project coordinators as eligible actors
- **THEN** that overview entry has `canComplete` set to `false`

#### Scenario: Coordinator overview covers every project with their Leistungsnachweis tasks
- **WHEN** a project coordinator requests their month-end status overview in the controlling capacity for a month in which they are eligible on `LEISTUNGSNACHWEIS` tasks of several projects
- **THEN** the system returns all tasks of each of those projects, including projects the coordinator does not lead
- **THEN** each `LEISTUNGSNACHWEIS` entry the coordinator is eligible on has `canComplete` set to `true`
- **THEN** `PROJECT_LEAD_REVIEW` and `ABRECHNUNG` entries of projects the coordinator does not lead have `canComplete` set to `false`

#### Scenario: Lead overview includes completed tasks
- **WHEN** a project lead requests their month-end status overview in the controlling capacity for a month in which some tasks are already done
- **THEN** the system returns both `OPEN` and `DONE` tasks for their projects

#### Scenario: Lead who is also an employee on their own project sees their personal tasks
- **WHEN** a project lead who is not a project coordinator is also assigned as an employee on a project they lead
- **THEN** their personal `EMPLOYEE_TIME_CHECK` and `LEISTUNGSNACHWEIS` tasks for that project appear in the overview in the controlling capacity
- **THEN** the `EMPLOYEE_TIME_CHECK` task has `canComplete` set to `false` (time checks are an employee duty)
- **THEN** the coordinator-eligible `LEISTUNGSNACHWEIS` task has `canComplete` set to `false` (they are not an eligible actor on it)

#### Scenario: Lead who is also an employee on their own project sees their own reviews as read-only in the employee capacity
- **WHEN** a project lead who is also assigned as an employee on a project they lead requests their overview in the employee capacity
- **THEN** their own `EMPLOYEE_TIME_CHECK` task for that project has `canComplete` set to `true`
- **THEN** the `PROJECT_LEAD_REVIEW` and `LEISTUNGSNACHWEIS` tasks about them for that project have `canComplete` set to `false`

### Requirement: Status overview entries expose the context needed for matrix or dashboard rendering
The system SHALL include the month-end task identity, task type, task status, a project reference object containing the project identifier and project name, and a nullable subject employee reference object containing the employee identifier and full name when present in each overview entry.

#### Scenario: Review task entry identifies the employee and named project
- **WHEN** the overview contains a `PROJECT_LEAD_REVIEW` task
- **THEN** that overview entry includes both a subject employee reference object and a project reference object
- **THEN** the subject employee reference object includes the associated employee identifier and full name
- **THEN** the project reference object includes the associated project identifier and project name

#### Scenario: Abrechnung entry identifies the named project without a subject employee
- **WHEN** the overview contains an `ABRECHNUNG` task
- **THEN** that overview entry includes a project reference object with the associated project identifier and project name
- **THEN** that overview entry includes no subject employee reference object

### Requirement: Status overview includes all clarifications related to the actor's scope for that month
The system SHALL include all `MonthEndClarification` records related to the actor's visible scope in the `MonthEndStatusOverview`, regardless of clarification status (`OPEN` or `DONE`):

- **Employee capacity**: all clarifications where the actor is the `subjectEmployeeId` for that month.
- **Controlling capacity**: all clarifications for projects the actor leads for that month (same project scope as the task query of that capacity).

Each clarification in the overview SHALL be represented as a `MonthEndOverviewClarificationItem` containing: clarification identity, project, a nested subject employee reference, a nested creator reference, creator side, status, text, `canResolve`, and, when `DONE`, resolution note, a nested resolver reference, and resolved timestamp.

`canResolve` SHALL be `true` if and only if the requesting actor can resolve that clarification according to the clarification's resolution rules: eligible project leads may resolve employee-created clarifications; the subject employee may resolve lead-created clarifications. `canResolve` SHALL be `false` for `DONE` clarifications. `canResolve` SHALL NOT depend on the requested capacity.

#### Scenario: Employee overview includes their open clarifications
- **WHEN** an actor requests their month-end status overview in the employee capacity for a month in which they have open clarifications as subject employee
- **THEN** the overview includes those clarifications with status `OPEN`

#### Scenario: Employee overview includes resolved clarifications
- **WHEN** an actor requests their month-end status overview in the employee capacity for a month in which some of their clarifications have been resolved
- **THEN** the overview includes those clarifications with status `DONE` and populated resolution fields

#### Scenario: canResolve reflects whether the actor can resolve each clarification
- **WHEN** the actor requests their overview in the employee capacity and has an open lead-created clarification scoped to them
- **THEN** that clarification item has `canResolve` set to `true`
- **WHEN** the actor requests their overview in the employee capacity and has an open employee-created clarification
- **THEN** that clarification item has `canResolve` set to `false`

#### Scenario: canResolve is false for done clarifications
- **WHEN** the overview contains a `DONE` clarification
- **THEN** that clarification item has `canResolve` set to `false`

#### Scenario: Lead overview includes all clarifications for their projects
- **WHEN** a project lead requests their month-end status overview in the controlling capacity for a month
- **THEN** the overview includes all clarifications (`OPEN` and `DONE`) for all projects the lead leads that month

#### Scenario: Lead can resolve employee-created clarifications they are eligible for
- **WHEN** the overview in the controlling capacity contains an open employee-created clarification for a project the lead leads
- **THEN** that clarification item has `canResolve` set to `true`

#### Scenario: Clarification flags are the same in both capacities
- **WHEN** a clarification appears in an actor's overview in both the employee and the controlling capacity
- **THEN** its `canResolve`, `canEditText`, and `canDelete` values are identical in both overviews

#### Scenario: Overview clarification items include resolution details for done clarifications
- **WHEN** a `DONE` clarification appears in the overview
- **THEN** the clarification item includes the resolver identity, resolution timestamp, and optional resolution note

#### Scenario: Overview clarification items expose nested user references
- **WHEN** a clarification appears in the overview
- **THEN** the clarification item includes nested user reference objects for the subject employee and creator
- **THEN** a resolved clarification also includes a nested user reference object for the resolver

### Requirement: Completed overview entries retain completion details
The system SHALL expose the completing actor for `DONE` month-end tasks in the status overview. Every overview entry SHALL carry the completing actor field regardless of task status; for an `OPEN` task the field SHALL be present with the value `null`.

#### Scenario: Shared task shows who completed it
- **WHEN** a shared lead-eligible month-end task appears as `DONE` in the status overview
- **THEN** the overview entry includes the eligible actor who completed that task

#### Scenario: Open task has no completer information
- **WHEN** an `OPEN` month-end task appears in the status overview
- **THEN** the overview entry includes the completing actor field with the value `null`

### Requirement: Status overview entries expose whether the actor can complete the task in the requested capacity
The system SHALL include a `canComplete` flag on each overview entry indicating whether the requesting actor can complete that task in the requested capacity. `canComplete` SHALL be `true` if and only if the actor is in `eligibleActorIds` for that task and the task type is a duty of the requested capacity. Being eligible alone SHALL NOT make a task completable in a capacity whose duty it is not.

#### Scenario: Eligible actor sees canComplete true for their own task
- **WHEN** an actor requests their status overview in the employee capacity and an entry corresponds to an `EMPLOYEE_TIME_CHECK` task where that actor is in `eligibleActorIds`
- **THEN** that overview entry has `canComplete` set to `true`

#### Scenario: Subject-only actor sees canComplete false for PROJECT_LEAD_REVIEW and LEISTUNGSNACHWEIS tasks
- **WHEN** an actor requests their status overview in the employee capacity and an entry corresponds to a `PROJECT_LEAD_REVIEW` or `LEISTUNGSNACHWEIS` task where they are the subject employee but not in `eligibleActorIds`
- **THEN** that overview entry has `canComplete` set to `false`

#### Scenario: Subject who is also eligible sees canComplete only in the capacity of the task's duty
- **WHEN** an actor is both the subject employee and in `eligibleActorIds` for a `PROJECT_LEAD_REVIEW` task
- **THEN** that task's overview entry has `canComplete` set to `false` in the employee capacity
- **THEN** that task's overview entry has `canComplete` set to `true` in the controlling capacity

#### Scenario: Read-only flag does not prevent completing the task
- **WHEN** an eligible actor's overview in the employee capacity shows a `PROJECT_LEAD_REVIEW` task about themselves with `canComplete` set to `false`
- **AND** that actor completes the task through single-task completion
- **THEN** the task is completed with that actor recorded as the completing actor
