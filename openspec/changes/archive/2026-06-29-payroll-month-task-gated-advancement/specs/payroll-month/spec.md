## MODIFIED Requirements

### Requirement: Employee payroll month resolves based on open monthend tasks
The system SHALL resolve the active payroll month for an authenticated employee by inspecting their open monthend tasks. If no open tasks exist for the previous month (including the case where no tasks have been generated yet), the system SHALL return the current calendar month. If open tasks exist for the current calendar month (indicating current-month task generation has occurred), the system SHALL also return the current calendar month. Otherwise, the system SHALL return the previous calendar month.

#### Scenario: Employee has open tasks in previous month only
- **WHEN** the authenticated employee has one or more open monthend tasks where they are the subject for the previous calendar month
- **AND** the authenticated employee has no open monthend tasks for the current calendar month
- **THEN** the resolved payroll month is the previous calendar month

#### Scenario: Employee has no open tasks in previous month
- **WHEN** the authenticated employee has no open monthend tasks where they are the subject for the previous calendar month
- **THEN** the resolved payroll month is the current calendar month

#### Scenario: No tasks have been generated yet for previous month
- **WHEN** no monthend tasks exist at all for the previous calendar month for the authenticated employee
- **THEN** the resolved payroll month is the current calendar month

#### Scenario: Employee has open tasks in previous month but current month tasks are generated
- **WHEN** the authenticated employee has one or more open monthend tasks where they are the subject for the previous calendar month
- **AND** the authenticated employee has one or more open monthend tasks for the current calendar month
- **THEN** the resolved payroll month is the current calendar month

### Requirement: Project-lead payroll month resolves based on current-month task availability
The system SHALL resolve the active payroll month for an authenticated project lead by inspecting whether monthend tasks have been generated for the current calendar month. If tasks exist for the project lead in the current month, the system SHALL return the current calendar month. Otherwise, the system SHALL return the previous calendar month.

#### Scenario: Current-month tasks have been generated for the project lead
- **WHEN** one or more monthend tasks exist for the authenticated project lead in the current calendar month
- **THEN** the resolved payroll month is the current calendar month

#### Scenario: No current-month tasks exist for the project lead
- **WHEN** no monthend tasks exist for the authenticated project lead in the current calendar month
- **THEN** the resolved payroll month is the previous calendar month

## REMOVED Requirements

### Requirement: Project-lead payroll month always resolves to previous month
**Reason**: Replaced by task-gated advancement — project leads now advance to the current month as soon as tasks are generated for them, preventing indefinite stagnation on the previous month.
**Migration**: No external API change; the response shape is unchanged. The resolved month may now be the current month where it previously was always the previous month.
