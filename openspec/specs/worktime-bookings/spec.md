# Work Time Bookings

## Purpose

Defines the detailed time-booking model of the `worktime` capability: the project-or-journey booking distinction, its per-slot detail (start, end, task, working location, project-relevance), and the mapping rules from raw ZEP attendance records to that vocabulary. Detailed bookings are the input the warning calculators require, in contrast to the aggregate work time report.

## Requirements

### Requirement: Detailed time bookings are queryable per employee for a payroll month
The system SHALL provide an outbound fetch that returns an employee's detailed time bookings for a given payroll month, derived from ZEP attendance records. Unlike the aggregate work-time attendance model (which carries only billable and non-billable hours), each detailed booking SHALL carry its start time, end time, task, working location, and project-relevance, so that the warning calculators have the per-slot detail they require. When the employee has no attendance records for the month, the result SHALL be an empty booking list.

#### Scenario: A regular attendance record becomes a project booking
- **WHEN** the fetch processes a ZEP attendance record whose activity is not a journey
- **THEN** the result contains a project booking carrying that record's start time, end time, task, working location, and project-relevance

#### Scenario: A journey attendance record becomes a journey booking
- **WHEN** the fetch processes a ZEP attendance record whose activity is a journey
- **THEN** the result contains a journey booking additionally carrying the journey direction and vehicle

#### Scenario: No attendances yields an empty booking list
- **WHEN** the fetch is invoked for a month in which the employee has no ZEP attendance records
- **THEN** the result is an empty booking list

### Requirement: Bookings are modelled as a project-or-journey distinction
The detailed booking model SHALL be a closed distinction of exactly two kinds: a project booking and a journey booking. Both kinds SHALL share start time, end time, task, working location, and project-relevance. A journey booking SHALL additionally carry a journey direction and a vehicle. A project booking SHALL additionally carry its process reference. Every booking SHALL expose its date, derived from the start time, and its duration in hours, derived from the start and end times.

#### Scenario: A project booking exposes date and duration
- **WHEN** a project booking is constructed
- **THEN** it exposes the date derived from its start time and the duration in hours between its start and end times

#### Scenario: A journey booking carries direction and vehicle
- **WHEN** a journey booking is constructed
- **THEN** it carries a journey direction and a vehicle in addition to the shared booking fields

### Requirement: ZEP attendance fields map to the booking vocabulary
The mapping from a ZEP attendance record to a booking SHALL apply these rules: the activity maps to a task, and a journey task selects the journey booking, otherwise the project booking; the direction of travel maps to the journey direction, defaulting to the outbound direction when absent; the vehicle identifier maps to a vehicle; the work-location code maps to a working location, defaulting to the main location when absent. A record whose activity or vehicle cannot be mapped to a known vocabulary value SHALL raise a mapping error rather than produce a booking. The task, working location, journey direction, and vehicle vocabularies SHALL be owned by the worktime bounded context (the mapping from raw ZEP codes lives in the outbound adapter).

#### Scenario: Known codes map to the correct vocabulary values
- **WHEN** a ZEP attendance record carries a known activity, vehicle, and work-location code
- **THEN** the produced booking carries the corresponding task, vehicle, and working location

#### Scenario: Missing optional codes fall back to defaults
- **WHEN** a journey attendance record has no direction of travel, or a record has no work-location code
- **THEN** the journey direction defaults to outbound and the working location defaults to the main location

#### Scenario: An unmappable required value raises a mapping error
- **WHEN** a ZEP attendance record carries an activity or vehicle that is not part of the worktime vocabulary
- **THEN** a mapping error is raised and no booking is produced for that record
