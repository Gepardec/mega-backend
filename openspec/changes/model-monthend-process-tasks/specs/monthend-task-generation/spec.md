## ADDED Requirements

### Requirement: Generation creates employee-owned month-end tasks per assigned project
The system SHALL generate employee-owned month-end tasks for every active employee assigned to an active project in the requested month. It MUST create an `EMPLOYEE_TIME_CHECK` task for every such assignment and a `LEISTUNGSNACHWEIS` task for every such assignment on a billable project.

#### Scenario: Billable project creates both employee tasks
- **WHEN** an active employee is assigned to a billable active project during month-end generation
- **THEN** the system creates one `EMPLOYEE_TIME_CHECK` task and one `LEISTUNGSNACHWEIS` task for that employee and project

#### Scenario: Non-billable project omits leistungsnachweis
- **WHEN** an active employee is assigned to a non-billable active project during month-end generation
- **THEN** the system creates one `EMPLOYEE_TIME_CHECK` task and no `LEISTUNGSNACHWEIS` task for that employee and project

### Requirement: Generation creates project-owned month-end tasks with eligible leads
The system SHALL generate project-owned month-end tasks for active projects using the leads assigned at generation time. It MUST create one `PROJECT_LEAD_REVIEW` task per active assigned employee and one `ABRECHNUNG` task per billable project, each with the eligible project leads captured at generation time.

#### Scenario: Lead review is generated once per employee
- **WHEN** an active project has active assigned employees and at least one active lead
- **THEN** the system creates one `PROJECT_LEAD_REVIEW` task per active assigned employee with all active project leads as eligible actors

#### Scenario: Abrechnung is generated once per billable project
- **WHEN** an active billable project has at least one active lead
- **THEN** the system creates one `ABRECHNUNG` task for the project with all active project leads as eligible actors

### Requirement: Generation is idempotent for an existing month
The system SHALL avoid duplicate month-end tasks when generation is rerun for the same month and business obligation.

#### Scenario: Regeneration keeps existing task instances
- **WHEN** month-end generation runs again for a month where the same project obligation already exists
- **THEN** the system does not create a duplicate `MonthEndTask` for that obligation

