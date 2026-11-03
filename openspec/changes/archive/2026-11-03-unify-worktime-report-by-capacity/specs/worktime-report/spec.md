## ADDED Requirements

### Requirement: Work time report entries have no defined order
The work time report SHALL NOT guarantee any order of its entries in either capacity. Grouping and sorting entries for display SHALL be a client concern.

#### Scenario: Client cannot rely on entry order
- **WHEN** the work time report contains several entries
- **THEN** the entries may be returned in any order
- **THEN** the set of entries and their values are the same regardless of order

## MODIFIED Requirements

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
