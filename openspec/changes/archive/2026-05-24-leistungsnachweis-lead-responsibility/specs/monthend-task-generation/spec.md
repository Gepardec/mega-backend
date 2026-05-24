## MODIFIED Requirements

### Requirement: Generation creates employee-owned month-end tasks per assigned project
The system SHALL generate employee-owned month-end tasks for every active employee assigned to an active project in the requested month. It MUST create an `EMPLOYEE_TIME_CHECK` task for every such assignment.

#### Scenario: Billable project creates employee time check task
- **WHEN** an active employee is assigned to a billable active project during month-end generation
- **THEN** the system creates one `EMPLOYEE_TIME_CHECK` task for that employee and project

#### Scenario: Non-billable project creates employee time check task
- **WHEN** an active employee is assigned to a non-billable active project during month-end generation
- **THEN** the system creates one `EMPLOYEE_TIME_CHECK` task for that employee and project

### Requirement: Generation creates project-owned month-end tasks with eligible leads
The system SHALL generate project-owned month-end tasks for active projects using the leads assigned at generation time. It MUST create one `PROJECT_LEAD_REVIEW` task per active assigned employee, one `LEISTUNGSNACHWEIS` task per active assigned employee on a billable project, and one `ABRECHNUNG` task per billable project, each with the eligible project leads captured at generation time.

#### Scenario: Lead review is generated once per employee
- **WHEN** an active project has active assigned employees and at least one active lead
- **THEN** the system creates one `PROJECT_LEAD_REVIEW` task per active assigned employee with all active project leads as eligible actors

#### Scenario: Leistungsnachweis is generated once per employee on a billable project
- **WHEN** an active billable project has active assigned employees and at least one active lead
- **THEN** the system creates one `LEISTUNGSNACHWEIS` task per active assigned employee with all active project leads as eligible actors

#### Scenario: Leistungsnachweis is not generated when billable project has no active leads
- **WHEN** an active billable project has active assigned employees but no active leads
- **THEN** the system does not create `LEISTUNGSNACHWEIS` tasks for that project

#### Scenario: Non-billable project does not generate Leistungsnachweis
- **WHEN** an active employee is assigned to a non-billable active project during month-end generation
- **THEN** the system does not create a `LEISTUNGSNACHWEIS` task for that assignment

#### Scenario: Abrechnung is generated once per billable project
- **WHEN** an active billable project has at least one active lead
- **THEN** the system creates one `ABRECHNUNG` task for the project with all active project leads as eligible actors
