# Spec Delta

## MODIFIED Requirements

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
