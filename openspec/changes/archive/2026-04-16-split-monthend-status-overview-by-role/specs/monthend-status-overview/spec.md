## MODIFIED Requirements

### Requirement: Status overview is derived from relevant month-end tasks for an actor and month
The system SHALL provide role-specific month-end status overviews, including both `OPEN` and `DONE` tasks:

- **Employee overview**: returns all `MonthEndTask` records where the employee is the `subjectEmployeeId`. This covers their own `EMPLOYEE_TIME_CHECK` and `LEISTUNGSNACHWEIS` tasks (where they are also eligible) and `PROJECT_LEAD_REVIEW` tasks about them (where they are subject but not eligible).
- **Lead overview**: returns all `MonthEndTask` records for projects the lead leads. A lead leads a project for a given month if they are in `eligibleActorIds` of any task for that project and month. This includes employee-owned tasks on those projects (visible but not actionable) as well as shared lead tasks (actionable).

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
