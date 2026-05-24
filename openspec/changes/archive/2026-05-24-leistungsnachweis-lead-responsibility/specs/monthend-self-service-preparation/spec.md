## MODIFIED Requirements

### Requirement: Employee can explicitly prepare their own month-end obligations across all assigned projects
The system SHALL allow the subject employee to explicitly prepare employee-owned month-end obligations for all projects they are assigned to in the given month, in a single operation, before the scheduled month-end generation runs. Preparation SHALL create the employee-owned `MonthEndTask` obligations that scheduled generation would create for each assigned project in the same month. The employee SHALL provide a non-blank clarification text as a mandatory reason for early preparation.

#### Scenario: Employee prepares all assigned projects including non-billable ones
- **WHEN** the subject employee prepares month-end obligations for a month in which they are assigned to a non-billable project
- **THEN** the system creates one `EMPLOYEE_TIME_CHECK` task for that month, project, and subject employee

#### Scenario: Employee prepares all assigned projects including billable ones
- **WHEN** the subject employee prepares month-end obligations for a month in which they are assigned to a billable project
- **THEN** the system creates one `EMPLOYEE_TIME_CHECK` task for that month, project, and subject employee
- **THEN** the system does not create a `LEISTUNGSNACHWEIS` task for that billable project context

#### Scenario: Employee assigned to multiple projects gets tasks for each
- **WHEN** the subject employee prepares month-end obligations for a month in which they are assigned to more than one project
- **THEN** the system creates the appropriate employee-owned tasks for each assigned project in the same operation
