# Month-End Self-Service Preparation

## Purpose

Defines the explicit employee-driven workflow for preparing employee-owned month-end obligations across all assigned projects before scheduled month-end generation runs.

## Requirements

### Requirement: Employee can explicitly prepare their own month-end obligations across all assigned projects
The system SHALL allow the subject employee to explicitly prepare employee-owned month-end obligations for all projects they are assigned to in the given month, in a single operation, before the scheduled month-end generation runs. Preparation SHALL create the employee-owned `MonthEndTask` obligations that scheduled generation would create for each assigned project in the same month. The employee SHALL provide a non-blank clarification text as a mandatory reason for early preparation.

#### Scenario: Employee prepares all assigned projects including non-billable ones
- **WHEN** the subject employee prepares month-end obligations for a month in which they are assigned to a non-billable project
- **THEN** the system creates one `EMPLOYEE_TIME_CHECK` task for that month, project, and subject employee
- **THEN** the system does not create a `LEISTUNGSNACHWEIS` task for that non-billable project context

#### Scenario: Employee prepares all assigned projects including billable ones
- **WHEN** the subject employee prepares month-end obligations for a month in which they are assigned to a billable project
- **THEN** the system creates one `EMPLOYEE_TIME_CHECK` task for that month, project, and subject employee
- **THEN** the system creates one `LEISTUNGSNACHWEIS` task for that month, project, and subject employee

#### Scenario: Employee assigned to multiple projects gets tasks for each
- **WHEN** the subject employee prepares month-end obligations for a month in which they are assigned to more than one project
- **THEN** the system creates the appropriate employee-owned tasks for each assigned project in the same operation

### Requirement: Self-service preparation is restricted to the subject employee acting on their own obligations
The system MUST allow self-service preparation only when the authenticated actor is the subject employee. The actor's assigned projects are discovered by the system; the actor MUST NOT supply a project identifier.

#### Scenario: Subject employee prepares their own obligations
- **WHEN** the authenticated employee submits a preparation request for a month
- **THEN** the system accepts the request and generates employee-owned tasks for all projects the employee is assigned to in that month

### Requirement: Self-service preparation is idempotent at the project-context level
The system SHALL skip preparation for any project context where employee-owned `MonthEndTask` obligations already exist for the given month, project, and subject employee. Skipped project contexts SHALL NOT receive a duplicate clarification.

#### Scenario: Already-prepared project context is skipped
- **WHEN** the subject employee prepares a month for which employee-owned tasks already exist for one of their assigned projects
- **THEN** the system does not create duplicate tasks for that project context
- **THEN** the system does not create a clarification for that project context

#### Scenario: Partial re-preparation only acts on unprepared contexts
- **WHEN** the subject employee prepares a month for which some assigned projects are already prepared and others are not
- **THEN** the system creates tasks and clarifications only for the project contexts that have no existing tasks

### Requirement: Self-service preparation creates a mandatory clarification for each newly prepared project context
The system SHALL require the subject employee to provide a non-blank clarification text when submitting a preparation request. For each project context in which new tasks are created, the system SHALL create one employee-created `MonthEndClarification` in that month, project, and subject employee context using the standard clarification rules, with the provided text applied to each clarification.

#### Scenario: Employee prepares multiple project contexts with clarification text
- **WHEN** the subject employee prepares a month in which they are assigned to multiple projects, all of which are unprepared
- **THEN** the system creates one `MonthEndClarification` per project context, each containing the provided clarification text
- **THEN** each clarification is routed to the eligible project leads of the respective project

#### Scenario: Clarification is not created for skipped project contexts
- **WHEN** the subject employee prepares a month for which some projects are already prepared
- **THEN** the system creates a clarification only for project contexts where new tasks were also created in the same operation
