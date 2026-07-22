## MODIFIED Requirements

### Requirement: Generation creates project-owned month-end tasks with eligible leads
The system SHALL generate project-owned month-end tasks for active projects using the leads assigned at generation time. It MUST create one `PROJECT_LEAD_REVIEW` task per active assigned employee, one `LEISTUNGSNACHWEIS` task per active assigned employee on a billable project **whose `leistungsnachweisEnabled` flag is true**, and one `ABRECHNUNG` task per billable project, each with the eligible project leads captured at generation time.

The `leistungsnachweisEnabled` flag SHALL be read from the project snapshot at generation time. A change to the flag SHALL only affect subsequent generation runs; it SHALL NOT retroactively add or remove already-generated `LEISTUNGSNACHWEIS` tasks. The flag SHALL gate `LEISTUNGSNACHWEIS` only; `PROJECT_LEAD_REVIEW` and `ABRECHNUNG` generation SHALL be unaffected by it.

#### Scenario: Lead review is generated once per employee
- **WHEN** an active project has active assigned employees and at least one active lead
- **THEN** the system creates one `PROJECT_LEAD_REVIEW` task per active assigned employee with all active project leads as eligible actors

#### Scenario: Leistungsnachweis is generated once per employee on an enabled billable project
- **WHEN** an active billable project has active assigned employees, at least one active lead, and `leistungsnachweisEnabled` is true
- **THEN** the system creates one `LEISTUNGSNACHWEIS` task per active assigned employee with all active project leads as eligible actors

#### Scenario: Leistungsnachweis is not generated when the project flag is disabled
- **WHEN** an active billable project has active assigned employees and at least one active lead but `leistungsnachweisEnabled` is false
- **THEN** the system does not create `LEISTUNGSNACHWEIS` tasks for that project
- **THEN** `PROJECT_LEAD_REVIEW` and `ABRECHNUNG` tasks are still generated for that project

#### Scenario: Leistungsnachweis is not generated when billable project has no active leads
- **WHEN** an active billable project has active assigned employees but no active leads
- **THEN** the system does not create `LEISTUNGSNACHWEIS` tasks for that project

#### Scenario: Non-billable project does not generate Leistungsnachweis
- **WHEN** an active employee is assigned to a non-billable active project during month-end generation
- **THEN** the system does not create a `LEISTUNGSNACHWEIS` task for that assignment

#### Scenario: Disabling the flag does not remove existing tasks
- **WHEN** a `LEISTUNGSNACHWEIS` task already exists for a project month and the project's `leistungsnachweisEnabled` flag is set to false afterwards
- **THEN** the existing `LEISTUNGSNACHWEIS` task is not removed
- **THEN** the next generation run for that month does not create additional `LEISTUNGSNACHWEIS` tasks for that project

#### Scenario: Abrechnung is generated once per billable project
- **WHEN** an active billable project has at least one active lead
- **THEN** the system creates one `ABRECHNUNG` task for the project with all active project leads as eligible actors
