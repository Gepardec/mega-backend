## ADDED Requirements

### Requirement: Domain service encapsulates worktime hour aggregation
The system SHALL provide a `WorkTimeReportAssembler` domain service in `worktime.domain.services` as a CDI `@ApplicationScoped` bean. It SHALL expose two operations used by both worktime application services:

- `totalHours(List<WorkTimeAttendance>) → double` — returns the sum of `totalHours` across all attendance records
- `buildEntry(UserRef, ProjectRef, List<WorkTimeAttendance>, double employeeMonthTotalHours) → WorkTimeEntry` — assembles a single `WorkTimeEntry` for one employee-project combination by summing billable and non-billable hours from the given attendance records

#### Scenario: totalHours sums all attendance records
- **WHEN** `WorkTimeReportAssembler.totalHours(attendances)` is called with a non-empty list
- **THEN** the returned value equals the sum of `WorkTimeAttendance.totalHours()` across all records

#### Scenario: totalHours returns zero for empty list
- **WHEN** `WorkTimeReportAssembler.totalHours(attendances)` is called with an empty list
- **THEN** the returned value is 0.0

#### Scenario: buildEntry calculates billable and non-billable hours correctly
- **WHEN** `WorkTimeReportAssembler.buildEntry(employee, project, projectAttendances, totalMonthHours)` is called
- **THEN** the returned `WorkTimeEntry.billableHours()` equals the sum of `WorkTimeAttendance.billableHours()` across `projectAttendances`
- **THEN** the returned `WorkTimeEntry.nonBillableHours()` equals the sum of `WorkTimeAttendance.nonBillableHours()` across `projectAttendances`
- **THEN** the returned `WorkTimeEntry.employeeMonthTotalHours()` equals the `employeeMonthTotalHours` argument passed in

#### Scenario: buildEntry carries the correct employee and project references
- **WHEN** `WorkTimeReportAssembler.buildEntry(employee, project, projectAttendances, totalMonthHours)` is called
- **THEN** the returned `WorkTimeEntry.employee()` equals the given `employee`
- **THEN** the returned `WorkTimeEntry.project()` equals the given `project`

### Requirement: Both worktime use cases delegate assembly to WorkTimeReportAssembler
`GetEmployeeWorkTimeService` and `GetProjectLeadWorkTimeService` SHALL NOT contain their own implementations of billable/non-billable hour summation or total hour calculation. Both SHALL inject and delegate to `WorkTimeReportAssembler` for all hour aggregation and entry construction.

#### Scenario: Employee worktime service produces correct entries via assembler
- **WHEN** `GetEmployeeWorkTimeUseCase` is invoked and attendance records exist for multiple projects
- **THEN** the returned `WorkTimeReport` entries match what `WorkTimeReportAssembler.buildEntry()` would produce for each employee-project group

#### Scenario: Project lead worktime service produces correct entries via assembler
- **WHEN** `GetProjectLeadWorkTimeUseCase` is invoked and employees have booked time on the lead's projects
- **THEN** the returned `WorkTimeReport` entries match what `WorkTimeReportAssembler.buildEntry()` would produce for each employee-project group
