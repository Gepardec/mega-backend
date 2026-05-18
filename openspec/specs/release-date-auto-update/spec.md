# Release Date Auto-Update

## Purpose

Defines the scheduled job that automatically advances employee release dates once all their month-end tasks for the previous payroll month are complete. The job runs daily from the 15th to the end of each month, retrying until all eligible employees have been processed.

## Requirements

### Requirement: Release dates are auto-updated once all month-end tasks are complete
The system SHALL run a scheduled job daily at 06:00 from the 15th to the end of each month. For each employee active during the previous payroll month, the job SHALL check whether all their month-end tasks for that payroll month are in `DONE` status. If all tasks are done, the job SHALL update the employee's release date to the last calendar day of the payroll month. Employees with one or more tasks in `OPEN` status SHALL be skipped and retried on the next daily run.

#### Scenario: All tasks done — release date updated
- **WHEN** the scheduler runs and an active employee has all month-end tasks for the previous payroll month in `DONE` status
- **THEN** the system updates that employee's release date to the last calendar day of the previous month

#### Scenario: Open tasks remain — employee skipped
- **WHEN** the scheduler runs and an active employee has at least one month-end task in `OPEN` status for the previous payroll month
- **THEN** the employee's release date is not updated
- **THEN** the employee will be reconsidered on the next daily run

#### Scenario: No tasks exist yet for the payroll month — employee skipped
- **WHEN** the scheduler runs and no month-end tasks have been generated yet for the previous payroll month for a given employee
- **THEN** the employee's release date is not updated

#### Scenario: Employees are processed independently
- **WHEN** the scheduler runs and employee A has all tasks in `DONE` status while employee B has a task in `OPEN` status
- **THEN** employee A's release date is updated
- **THEN** employee B is skipped without affecting employee A

### Requirement: A failed update for one employee does not block others
The system SHALL continue processing remaining employees if an individual release date update fails. A partial run SHALL be acceptable; affected employees will be retried on the next daily execution.

#### Scenario: Update failure for one employee is isolated
- **WHEN** a release date update fails for one employee during the scheduled run
- **THEN** the failure is recorded and processing continues for all remaining employees

### Requirement: The auto-updated release date is always the last day of the payroll month
The system SHALL always set the release date to the last calendar day of the previous month. This value is not configurable.

#### Scenario: Correct release date computed for a 30-day month
- **WHEN** the scheduler runs in May 2025
- **THEN** the release date set for eligible employees is 30 April 2025

#### Scenario: Correct release date computed for February
- **WHEN** the scheduler runs in March 2025
- **THEN** the release date set for eligible employees is 28 February 2025
