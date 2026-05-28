# Work Time Report

## Purpose

Defines the work time reporting capability that aggregates billable and non-billable hours per payroll month for employees and project leads.

## Requirements

### Requirement: Work time is queryable per employee for a payroll month
The system SHALL provide a use case that returns a flat list of work time entries for a given employee and payroll month. Each entry SHALL represent the aggregated billable and non-billable hours that employee booked against a single project in that month. Each entry SHALL also carry the employee's total hours across ALL their projects for that month. Entries SHALL be derived from ZEP attendance records using the per-record `billable` flag.

#### Scenario: Employee retrieves their work time for a month
- **WHEN** `GetEmployeeWorkTimeUseCase` is invoked with a valid `UserId` and `YearMonth`
- **THEN** the use case returns a `WorkTimeReport` containing one `WorkTimeEntry` per project the employee booked time on in that month
- **THEN** each entry contains the sum of billable hours and the sum of non-billable hours for that employee-project combination derived from the ZEP attendance `billable` flag
- **THEN** each entry contains `employeeMonthTotalHours` equal to the sum of all the employee's hours across all projects that month

#### Scenario: Employee with no bookings in that month receives an empty report
- **WHEN** `GetEmployeeWorkTimeUseCase` is invoked for a month with no ZEP attendances
- **THEN** the use case returns a `WorkTimeReport` with an empty entries list

### Requirement: Work time is queryable for all projects a lead manages
The system SHALL provide a use case that returns a flat list of work time entries covering ALL projects where the calling user is a project lead, for a given payroll month. The project scope SHALL be determined by `Project.leads` - all projects where the caller's `UserId` is in the leads set. Each entry SHALL represent the aggregated billable and non-billable hours a single employee booked against a single project in that month. Each entry SHALL carry the employee's total hours across ALL their projects for that month to enable correct percentage computation.

#### Scenario: Project lead retrieves work time across all their projects
- **WHEN** `GetProjectLeadWorkTimeUseCase` is invoked with a `UserId` and `YearMonth`
- **THEN** the use case resolves all projects where the caller is a lead via `Project.leads`
- **THEN** the use case returns a `WorkTimeReport` containing one `WorkTimeEntry` per (employee x project) combination where the employee booked time on one of the lead's projects that month
- **THEN** each entry contains the sum of billable hours and the sum of non-billable hours for that employee-project combination
- **THEN** each entry contains `employeeMonthTotalHours` equal to the sum of all that employee's hours across ALL their projects that month (not limited to the lead's projects)

#### Scenario: Project lead with no managed projects receives an empty report
- **WHEN** `GetProjectLeadWorkTimeUseCase` is invoked for a caller who leads no projects
- **THEN** the use case returns a `WorkTimeReport` with an empty entries list

#### Scenario: No bookings on any of the lead's projects results in an empty report
- **WHEN** `GetProjectLeadWorkTimeUseCase` is invoked and no employees booked time on any of the lead's projects that month
- **THEN** the use case returns a `WorkTimeReport` with an empty entries list

### Requirement: WorkTimeReport is a flat (employee x project) model
The `WorkTimeReport` domain model SHALL hold a `YearMonth` and a list of `WorkTimeEntry` records. Each `WorkTimeEntry` SHALL contain a `WorkTimeEmployee` (UserId, display name), a `WorkTimeProject` (ProjectId, display name), `billableHours` (double), `nonBillableHours` (double), and `employeeMonthTotalHours` (double). The model SHALL NOT include pre-computed totals or percentages.

#### Scenario: WorkTimeEntry holds the correct fields
- **WHEN** a `WorkTimeEntry` is constructed
- **THEN** it contains `WorkTimeEmployee`, `WorkTimeProject`, `billableHours`, `nonBillableHours`, and `employeeMonthTotalHours`
- **THEN** it does NOT contain derived fields such as total hours per entry or percentage
