# Month-End Self-Service Preparation

## Purpose

Defines the explicit employee-driven workflow for preparing employee-owned month-end obligations for one project context before scheduled month-end generation runs.

## Requirements

### Requirement: Employee can explicitly prepare their own project month-end obligations
The system SHALL allow the subject employee to explicitly prepare month-end obligations for one project context and month at a time before the scheduled month-end generation runs. Preparation SHALL create the employee-owned `MonthEndTask` obligations that scheduled generation would create for the same month, project, and subject employee.

#### Scenario: Employee prepares a non-billable project context
- **WHEN** the subject employee prepares month-end obligations for a non-billable project in a month-end context
- **THEN** the system creates one `EMPLOYEE_TIME_CHECK` task for that month, project, and subject employee
- **THEN** the system does not create a `LEISTUNGSNACHWEIS` task for that context

#### Scenario: Employee prepares a billable project context
- **WHEN** the subject employee prepares month-end obligations for a billable project in a month-end context
- **THEN** the system creates one `EMPLOYEE_TIME_CHECK` task for that month, project, and subject employee
- **THEN** the system creates one `LEISTUNGSNACHWEIS` task for that month, project, and subject employee

### Requirement: Self-service preparation is restricted to the subject employee and their project context
The system MUST allow self-service preparation only when the authenticated actor is the subject employee of the prepared month-end project context and is eligible for the corresponding employee-owned obligations.

#### Scenario: Subject employee prepares their own project context
- **WHEN** the authenticated employee prepares month-end obligations for a project context where they are the subject employee
- **THEN** the system accepts the preparation request

#### Scenario: Another actor cannot prepare an employee's project context
- **WHEN** a user that is not the subject employee attempts to prepare month-end obligations for that employee's project context
- **THEN** the system rejects the preparation request

### Requirement: Self-service preparation is idempotent per employee-owned business obligation
The system SHALL ensure that explicit preparation does not create duplicate employee-owned `MonthEndTask` obligations for the same month, project, task type, and subject employee.

#### Scenario: Repeating preparation keeps the same business obligations
- **WHEN** the subject employee prepares the same project month-end context more than once for the same month
- **THEN** the system does not create duplicate `EMPLOYEE_TIME_CHECK` or `LEISTUNGSNACHWEIS` tasks for that business obligation

### Requirement: Self-service preparation can optionally create a clarification in the same context
The system SHALL allow the subject employee to provide clarification text while preparing a project month-end context. When clarification text is provided, the system SHALL create an employee-created `MonthEndClarification` in the same month, project, and subject employee context using the standard clarification rules.

#### Scenario: Employee prepares a project context with clarification text
- **WHEN** the subject employee prepares a project month-end context and provides clarification text
- **THEN** the system ensures the employee-owned `MonthEndTask` obligations exist for that context
- **THEN** the system creates an open employee-created `MonthEndClarification` for the same month, project, and subject employee context
