## ADDED Requirements

### Requirement: Employee payroll month resolves based on open monthend tasks
The system SHALL resolve the active payroll month for an authenticated employee by inspecting their open monthend tasks for the previous calendar month. If no open tasks exist for the previous month (including the case where no tasks have been generated yet), the system SHALL return the current calendar month. Otherwise, the system SHALL return the previous calendar month.

#### Scenario: Employee has open tasks in previous month
- **WHEN** the authenticated employee has one or more open monthend tasks where they are the subject for the previous calendar month
- **THEN** the resolved payroll month is the previous calendar month

#### Scenario: Employee has no open tasks in previous month
- **WHEN** the authenticated employee has no open monthend tasks where they are the subject for the previous calendar month
- **THEN** the resolved payroll month is the current calendar month

#### Scenario: No tasks have been generated yet for previous month
- **WHEN** no monthend tasks exist at all for the previous calendar month for the authenticated employee
- **THEN** the resolved payroll month is the current calendar month

### Requirement: Project-lead payroll month always resolves to previous month
The system SHALL resolve the active payroll month for an authenticated project lead as the previous calendar month, unconditionally. No task state is consulted.

#### Scenario: Project lead requests their payroll month
- **WHEN** an authenticated project lead requests the payroll month
- **THEN** the resolved payroll month is the previous calendar month regardless of any task state

### Requirement: Payroll month resolution does not apply a calendar-day gate
The system SHALL NOT apply any day-of-month threshold when resolving the payroll month. The resolution SHALL depend only on task completion state (for employees) or be unconditional (for project leads).

#### Scenario: Employee completes all tasks before the 14th
- **WHEN** the authenticated employee has no open tasks for the previous month and today is before the 14th of the current month
- **THEN** the resolved payroll month is still the current calendar month
