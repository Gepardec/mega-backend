# Month-End Status Overview

## Purpose

Defines a status-oriented month-end overview for matrix or dashboard-style views that need visibility into both open and completed month-end obligations.

## Requirements

### Requirement: Status overview is derived from relevant month-end tasks for an actor and month
The system SHALL provide role-specific month-end status overviews, including both `OPEN` and `DONE` tasks:

- **Employee overview**: returns all `MonthEndTask` records where the employee is the `subjectEmployeeId`. This covers their own `EMPLOYEE_TIME_CHECK` and `LEISTUNGSNACHWEIS` tasks (where they are also eligible) and `PROJECT_LEAD_REVIEW` tasks about them (where they are subject but not eligible).
- **Lead overview**: returns all `MonthEndTask` records for projects the lead leads. A lead leads a project for a given month if they are in `eligibleActorIds` of any task for that project and month. This includes employee-owned tasks on those projects (visible but not actionable) as well as shared lead tasks (actionable).

The use case SHALL return a `MonthEndStatusOverview` containing raw `MonthEndTask` domain objects in `entries`. No application service SHALL pre-enrich tasks with display references (`ProjectRef`, `UserRef`) or compute actor-specific flags. Task display enrichment — resolving project and user identifiers to display references and evaluating `canComplete` — is NOT a domain or application concern and SHALL be performed exclusively by the REST adapter.

`canComplete` SHALL be `true` if and only if the actor is in `eligibleActorIds` for that task, regardless of role.

#### Scenario: Employee overview includes open and completed employee-owned tasks
- **WHEN** an employee requests their month-end status overview for a month in which some of their employee-owned month-end tasks are open and others are done
- **THEN** the system returns both the open and completed employee-owned tasks in the overview

#### Scenario: Employee overview includes PROJECT_LEAD_REVIEW tasks about that employee
- **WHEN** an employee requests their month-end status overview for a month that contains `PROJECT_LEAD_REVIEW` tasks where that employee is the subject
- **THEN** the system returns those `PROJECT_LEAD_REVIEW` tasks in the employee's overview

#### Scenario: Employee overview does not include ABRECHNUNG tasks
- **WHEN** an employee requests their month-end status overview
- **THEN** the system does not return `ABRECHNUNG` tasks, as those have no subject employee

#### Scenario: Employee sees at most one entry per task even when subject and eligible
- **WHEN** an employee requests their month-end status overview and has tasks where they are both the subject employee and in the eligible actor set
- **THEN** each such task appears exactly once in the overview

#### Scenario: Lead overview includes all task types for their projects
- **WHEN** a project lead requests their month-end status overview for a month
- **THEN** the system returns `EMPLOYEE_TIME_CHECK`, `LEISTUNGSNACHWEIS`, `PROJECT_LEAD_REVIEW`, and `ABRECHNUNG` tasks for all projects the lead leads

#### Scenario: Lead overview does not include tasks from projects they do not lead
- **WHEN** a project lead requests their month-end status overview for a month
- **THEN** the system does not return tasks from projects where the lead has no eligible-actor role

#### Scenario: Lead sees employee-owned tasks as read-only
- **WHEN** the lead overview contains an `EMPLOYEE_TIME_CHECK` or `LEISTUNGSNACHWEIS` task
- **THEN** that overview entry has `canComplete` set to `false`

#### Scenario: Lead overview includes completed tasks
- **WHEN** a project lead requests their month-end status overview for a month in which some tasks are already done
- **THEN** the system returns both `OPEN` and `DONE` tasks for their projects

#### Scenario: Lead who is also an employee on their own project sees their personal tasks
- **WHEN** a project lead is also assigned as an employee on a project they lead
- **THEN** their personal `EMPLOYEE_TIME_CHECK` and `LEISTUNGSNACHWEIS` tasks for that project appear in the lead overview

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

### Requirement: Status overview entries expose whether the actor can complete the task
The system SHALL include a `canComplete` flag on each overview entry indicating whether the requesting actor is eligible to complete that task. `canComplete` SHALL be `true` if and only if the actor is in `eligibleActorIds` for that task. The REST adapter SHALL compute `canComplete` by calling `task.canBeCompletedBy(actorId)` — a domain method on `MonthEndTask` that returns `true` iff `actorId` is in `eligibleActorIds`.

#### Scenario: Eligible actor sees canComplete true for their own task
- **WHEN** an employee requests their status overview and an entry corresponds to a task where that employee is in `eligibleActorIds`
- **THEN** that overview entry has `canComplete` set to `true`

#### Scenario: Subject-only actor sees canComplete false for a PROJECT_LEAD_REVIEW task
- **WHEN** an employee requests their status overview and an entry corresponds to a `PROJECT_LEAD_REVIEW` task where they are the subject employee but not in `eligibleActorIds`
- **THEN** that overview entry has `canComplete` set to `false`

#### Scenario: Eligible always wins when actor is both subject and eligible
- **WHEN** an actor is both the subject employee and in `eligibleActorIds` for a task
- **THEN** that overview entry has `canComplete` set to `true`

### Requirement: Status overview includes all clarifications related to the actor's scope for that month
The system SHALL include all `MonthEndClarification` records related to the actor's visible scope in the `MonthEndStatusOverview`, regardless of clarification status (`OPEN` or `DONE`):

- **Employee overview**: all clarifications where the employee is the `subjectEmployeeId` for that month.
- **Lead overview**: all clarifications for projects the lead leads for that month (same project scope as the lead's task query).

Each clarification in the overview SHALL be represented as a `MonthEndOverviewClarificationItem` containing: clarification identity, project, a nested subject employee reference, a nested creator reference, creator side, status, text, `canResolve`, and, when `DONE`, resolution note, a nested resolver reference, and resolved timestamp.

`canResolve` SHALL be `true` if and only if the requesting actor can resolve that clarification according to the clarification's resolution rules: eligible project leads may resolve employee-created clarifications; the subject employee may resolve lead-created clarifications. `canResolve` SHALL be `false` for `DONE` clarifications.

#### Scenario: Employee overview includes their open clarifications
- **WHEN** an employee requests their month-end status overview for a month in which they have open clarifications as subject employee
- **THEN** the overview includes those clarifications with status `OPEN`

#### Scenario: Employee overview includes resolved clarifications
- **WHEN** an employee requests their month-end status overview for a month in which some of their clarifications have been resolved
- **THEN** the overview includes those clarifications with status `DONE` and populated resolution fields

#### Scenario: canResolve reflects whether the actor can resolve each clarification
- **WHEN** the employee requests their overview and has an open lead-created clarification scoped to them
- **THEN** that clarification item has `canResolve` set to `true`
- **WHEN** the employee requests their overview and has an open employee-created clarification
- **THEN** that clarification item has `canResolve` set to `false`

#### Scenario: canResolve is false for done clarifications
- **WHEN** the overview contains a `DONE` clarification
- **THEN** that clarification item has `canResolve` set to `false`

#### Scenario: Lead overview includes all clarifications for their projects
- **WHEN** a project lead requests their month-end status overview for a month
- **THEN** the overview includes all clarifications (`OPEN` and `DONE`) for all projects the lead leads that month

#### Scenario: Lead can resolve employee-created clarifications they are eligible for
- **WHEN** the lead overview contains an open employee-created clarification for a project the lead leads
- **THEN** that clarification item has `canResolve` set to `true`

#### Scenario: Overview clarification items include resolution details for done clarifications
- **WHEN** a `DONE` clarification appears in the overview
- **THEN** the clarification item includes the resolver identity, resolution timestamp, and optional resolution note

#### Scenario: Overview clarification items expose nested user references
- **WHEN** a clarification appears in the overview
- **THEN** the clarification item includes nested user reference objects for the subject employee and creator
- **THEN** a resolved clarification also includes a nested user reference object for the resolver

### Requirement: Completed overview entries retain completion details
The system SHALL expose the completing actor for `DONE` month-end tasks in the status overview.

#### Scenario: Shared task shows who completed it
- **WHEN** a shared lead-eligible month-end task appears as `DONE` in the status overview
- **THEN** the overview entry includes the eligible actor who completed that task

#### Scenario: Open task has no completer information
- **WHEN** an `OPEN` month-end task appears in the status overview
- **THEN** the overview entry does not include a completing actor

