# Month-End Task Generation

## Purpose

Defines how month-end obligations are generated from active projects, active users, project assignments, and captured lead eligibility, including idempotent regeneration and scheduled triggering.

## Requirements

### Requirement: Generation creates employee-owned month-end tasks per assigned project
The system SHALL generate employee-owned month-end tasks for every active employee assigned to an active project in the requested month. It MUST create an `EMPLOYEE_TIME_CHECK` task for every such assignment.

#### Scenario: Billable project creates employee time check task
- **WHEN** an active employee is assigned to a billable active project during month-end generation
- **THEN** the system creates one `EMPLOYEE_TIME_CHECK` task for that employee and project

#### Scenario: Non-billable project creates employee time check task
- **WHEN** an active employee is assigned to a non-billable active project during month-end generation
- **THEN** the system creates one `EMPLOYEE_TIME_CHECK` task for that employee and project

### Requirement: Generation creates project-owned month-end tasks with eligible leads
The system SHALL generate project-owned month-end tasks for active projects. It MUST create one `PROJECT_LEAD_REVIEW` task per active assigned employee and one `ABRECHNUNG` task per billable project, each with the eligible project leads captured at generation time. It MUST create one `LEISTUNGSNACHWEIS` task per active assigned employee on a billable project **whose `leistungsnachweisEnabled` flag is true** and that has at least one active lead, with the active project coordinators captured at generation time as its eligible actors. Project leads SHALL NOT be eligible actors on newly generated `LEISTUNGSNACHWEIS` tasks unless they are also project coordinators.

A project coordinator is a user holding the project coordinator role who is active in the generated month. Every active project coordinator SHALL be an eligible actor on every generated `LEISTUNGSNACHWEIS` task, regardless of which projects they lead.

The `leistungsnachweisEnabled` flag SHALL be read from the project snapshot at generation time. A change to the flag SHALL only affect subsequent generation runs; it SHALL NOT retroactively add or remove already-generated `LEISTUNGSNACHWEIS` tasks. The flag SHALL gate `LEISTUNGSNACHWEIS` only; `PROJECT_LEAD_REVIEW` and `ABRECHNUNG` generation SHALL be unaffected by it. Changes to who holds the project coordinator role SHALL NOT alter the eligible actors of already-generated tasks.

#### Scenario: Lead review is generated once per employee
- **WHEN** an active project has active assigned employees and at least one active lead
- **THEN** the system creates one `PROJECT_LEAD_REVIEW` task per active assigned employee with all active project leads as eligible actors

#### Scenario: Leistungsnachweis is generated once per employee on a billable project
- **WHEN** an active billable project has active assigned employees, at least one active lead, `leistungsnachweisEnabled` is true, and at least one active project coordinator exists
- **THEN** the system creates one `LEISTUNGSNACHWEIS` task per active assigned employee with all active project coordinators as eligible actors

#### Scenario: Project leads are not eligible on Leistungsnachweis
- **WHEN** the system creates a `LEISTUNGSNACHWEIS` task for a project whose lead does not hold the project coordinator role
- **THEN** that lead is not an eligible actor on the task

#### Scenario: A coordinator who leads the project is eligible once
- **WHEN** an active project coordinator is also an active lead of the project
- **THEN** the generated `LEISTUNGSNACHWEIS` tasks of that project have that coordinator as an eligible actor
- **THEN** the generated `PROJECT_LEAD_REVIEW` and `ABRECHNUNG` tasks of that project have that coordinator as an eligible actor in their lead capacity

#### Scenario: Several coordinators are all eligible
- **WHEN** two active project coordinators exist at generation time
- **THEN** every generated `LEISTUNGSNACHWEIS` task has both coordinators as eligible actors

#### Scenario: Inactive coordinator is not eligible
- **WHEN** a user holds the project coordinator role but is not active in the generated month
- **THEN** that user is not an eligible actor on the generated `LEISTUNGSNACHWEIS` tasks

#### Scenario: Leistungsnachweis is not generated when the project flag is disabled
- **WHEN** an active billable project has active assigned employees and at least one active lead but `leistungsnachweisEnabled` is false
- **THEN** the system does not create `LEISTUNGSNACHWEIS` tasks for that project
- **THEN** `PROJECT_LEAD_REVIEW` and `ABRECHNUNG` tasks are still generated for that project

#### Scenario: Leistungsnachweis is not generated when billable project has no active leads
- **WHEN** an active billable project has active assigned employees but no active leads
- **THEN** the system does not create `LEISTUNGSNACHWEIS` tasks for that project, even when active project coordinators exist

#### Scenario: Non-billable project does not generate Leistungsnachweis
- **WHEN** an active employee is assigned to a non-billable active project during month-end generation
- **THEN** the system does not create a `LEISTUNGSNACHWEIS` task for that assignment

#### Scenario: Disabling the flag does not remove existing tasks
- **WHEN** a `LEISTUNGSNACHWEIS` task already exists for a project month and the project's `leistungsnachweisEnabled` flag is set to false afterwards
- **THEN** the existing `LEISTUNGSNACHWEIS` task is not removed
- **THEN** the next generation run for that month does not create additional `LEISTUNGSNACHWEIS` tasks for that project

#### Scenario: Existing Leistungsnachweis tasks keep their eligible actors
- **WHEN** a `LEISTUNGSNACHWEIS` task was generated with project leads as eligible actors before project coordinators took over Leistungsnachweis
- **THEN** that task keeps its project leads as eligible actors
- **THEN** a later generation run for the same month does not create another `LEISTUNGSNACHWEIS` task for the same project and employee

#### Scenario: Abrechnung is generated once per billable project
- **WHEN** an active billable project has at least one active lead
- **THEN** the system creates one `ABRECHNUNG` task for the project with all active project leads as eligible actors

### Requirement: Generation is idempotent for an existing month
The system SHALL avoid duplicate month-end tasks when generation is rerun for the same month and business obligation, regardless of whether the existing obligation was created by scheduled generation or by explicit employee self-service preparation.

#### Scenario: Regeneration keeps existing task instances
- **WHEN** month-end generation runs again for a month where the same project obligation already exists
- **THEN** the system does not create a duplicate `MonthEndTask` for that obligation

#### Scenario: Scheduled generation skips an employee task prepared through self-service
- **WHEN** scheduled month-end generation runs for a month where an employee-owned obligation with the same business key was already prepared explicitly by the subject employee
- **THEN** the system does not create a duplicate `MonthEndTask` for that obligation

### Requirement: Scheduled generation is the primary driver of month-end task creation
The system SHALL trigger month-end task generation through a scheduled method on the last **working** day of the month for the current month. The scheduler SHALL fire daily during the last week of the month (days 25–31) and skip execution on any day that is not the last working day, accounting for weekends and Austrian public holidays.

#### Scenario: Scheduled trigger runs on last working day
- **WHEN** the scheduler fires and today is the last working day of the current month
- **THEN** the scheduler invokes month-end task generation for that current `YearMonth`

#### Scenario: Scheduled trigger skips on a non-last-working day
- **WHEN** the scheduler fires and today is not the last working day of the current month
- **THEN** the scheduler exits without invoking month-end task generation

### Requirement: Generation without an active project coordinator skips Leistungsnachweis
When no active project coordinator exists at generation time, the system SHALL generate all other month-end tasks as usual, SHALL NOT create any `LEISTUNGSNACHWEIS` task, and SHALL log an error naming the month. No further alerting is required. A later generation run for the same month, once a coordinator exists, SHALL create the missing `LEISTUNGSNACHWEIS` tasks.

#### Scenario: No coordinator configured
- **WHEN** month-end generation runs and no active user holds the project coordinator role
- **THEN** the system creates `EMPLOYEE_TIME_CHECK`, `PROJECT_LEAD_REVIEW`, and `ABRECHNUNG` tasks as usual
- **THEN** the system creates no `LEISTUNGSNACHWEIS` task
- **THEN** the system logs an error stating that Leistungsnachweis tasks were skipped for that month because no project coordinator exists

#### Scenario: Rerun after a coordinator is configured fills in Leistungsnachweis
- **WHEN** generation for a month ran without an active project coordinator and runs again for that month after a coordinator has been configured and synced
- **THEN** the system creates the `LEISTUNGSNACHWEIS` tasks that qualify for that month with the coordinator as eligible actor
- **THEN** the system does not duplicate any task created by the earlier run
