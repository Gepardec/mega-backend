# Spec Delta

## ADDED Requirements

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

## REMOVED Requirements

### Requirement: Work time report entries have no defined order
**Reason**: The frontend groups and displays entries in the order the API returns them. Unordered entries jump around between reloads and break the per-employee grouping in the controlling view.
**Migration**: None. Clients that already sort on their own keep working. Clients that relied on the pre-refactor order get it back.
