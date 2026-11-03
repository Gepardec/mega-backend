# Work Time Report

## Purpose

Defines the work time reporting capability that aggregates billable and non-billable hours per payroll month for employees and project leads.

## Requirements

### Requirement: Work time is queryable per employee for a payroll month
The system SHALL return, for the employee capacity, a flat list of work time entries for the acting employee and payroll month. Each entry SHALL represent the aggregated billable and non-billable hours that employee booked against a single project in that month. Each entry SHALL also carry the employee's total hours across ALL their projects for that month. Entries SHALL be derived from ZEP attendance records using the per-record `billable` flag.

#### Scenario: Employee retrieves their work time for a month
- **WHEN** the work time report is requested in the employee capacity for an employee and month
- **THEN** the report contains one entry per project the employee booked time on in that month
- **THEN** each entry contains the sum of billable hours and the sum of non-billable hours for that employee-project combination derived from the ZEP attendance `billable` flag
- **THEN** each entry contains `employeeMonthTotalHours` equal to the sum of all the employee's hours across all projects that month

#### Scenario: Employee with no bookings in that month receives an empty report
- **WHEN** the work time report is requested in the employee capacity for a month with no ZEP attendances for that employee
- **THEN** the report has an empty entries list

### Requirement: Work time is queryable for all projects a lead manages
The system SHALL return, for the controlling capacity, a flat list of work time entries covering ALL projects where the acting user is a project lead, for a given payroll month. The project scope SHALL be determined by the projects' current lead assignments for that month. Each entry SHALL represent the aggregated billable and non-billable hours a single employee booked against a single project in that month. Each entry SHALL carry the employee's total hours across ALL their projects for that month to enable correct percentage computation.

#### Scenario: Project lead retrieves work time across all their projects
- **WHEN** the work time report is requested in the controlling capacity for a lead and month
- **THEN** the report covers all projects where the caller is a lead in that month
- **THEN** the report contains one entry per (employee x project) combination where the employee booked time on one of the lead's projects that month
- **THEN** each entry contains the sum of billable hours and the sum of non-billable hours for that employee-project combination
- **THEN** each entry contains `employeeMonthTotalHours` equal to the sum of all that employee's hours across ALL their projects that month (not limited to the lead's projects)

#### Scenario: Project lead with no managed projects receives an empty report
- **WHEN** the work time report is requested in the controlling capacity for a caller who leads no projects
- **THEN** the report has an empty entries list

#### Scenario: No bookings on any of the lead's projects results in an empty report
- **WHEN** the work time report is requested in the controlling capacity and no employees booked time on any of the lead's projects that month
- **THEN** the report has an empty entries list

#### Scenario: Lead's own bookings appear in both capacities
- **WHEN** a project lead booked time on a project they lead and on another project they do not lead
- **THEN** the report in the employee capacity contains an entry for each of the two projects
- **THEN** the report in the controlling capacity contains the lead's entry only for the project they lead
- **THEN** that entry carries the same hours and `employeeMonthTotalHours` in both reports

### Requirement: WorkTimeReport is a flat (employee x project) model
The `WorkTimeReport` domain model SHALL hold a `YearMonth` and a list of `WorkTimeEntry` records. Each `WorkTimeEntry` SHALL contain a `WorkTimeEmployee` (UserId, display name), a `WorkTimeProject` (ProjectId, display name), `billableHours` (double), `nonBillableHours` (double), and `employeeMonthTotalHours` (double). The model SHALL NOT include pre-computed totals or percentages.

#### Scenario: WorkTimeEntry holds the correct fields
- **WHEN** a `WorkTimeEntry` is constructed
- **THEN** it contains `WorkTimeEmployee`, `WorkTimeProject`, `billableHours`, `nonBillableHours`, and `employeeMonthTotalHours`
- **THEN** it does NOT contain derived fields such as total hours per entry or percentage

### Requirement: Work time report entries are ordered by employee and project
The work time report SHALL return its entries sorted by employee display name and then by project name, in both capacities. The order SHALL be the same on every request for the same data.

#### Scenario: Controlling report is ordered by employee, then project
- **WHEN** the work time report is requested in the controlling capacity and it contains entries for several employees and projects
- **THEN** the entries are sorted by employee display name
- **THEN** entries of the same employee are sorted by project name

#### Scenario: Employee report is ordered by project
- **WHEN** the work time report is requested in the employee capacity and the employee booked time on several projects
- **THEN** the entries are sorted by project name

#### Scenario: Repeated requests return the same order
- **WHEN** the work time report is requested twice for the same capacity, caller, and month with unchanged bookings
- **THEN** both responses list the entries in the same order
