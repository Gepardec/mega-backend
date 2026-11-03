# Spec Delta

## MODIFIED Requirements

### Requirement: Detailed time bookings are queryable per employee for a payroll month
The system SHALL provide an outbound fetch that returns an employee's detailed time bookings for a given payroll month, derived from ZEP attendance records. Unlike the aggregate work-time attendance model (which carries only billable and non-billable hours), each detailed booking SHALL carry its start time, end time, task, working location, project-relevance, booked project, and note, so that the warning calculators have the per-slot detail they require. When the employee has no attendance records for the month, the result SHALL be an empty booking list.

#### Scenario: A regular attendance record becomes a project booking
- **WHEN** the fetch processes a ZEP attendance record whose activity is not a journey
- **THEN** the result contains a project booking carrying that record's start time, end time, task, working location, project-relevance, booked project, and note

#### Scenario: A journey attendance record becomes a journey booking
- **WHEN** the fetch processes a ZEP attendance record whose activity is a journey
- **THEN** the result contains a journey booking additionally carrying the journey direction and vehicle

#### Scenario: No attendances yields an empty booking list
- **WHEN** the fetch is invoked for a month in which the employee has no ZEP attendance records
- **THEN** the result is an empty booking list

### Requirement: Bookings are modelled as a project-or-journey distinction
The detailed booking model SHALL be a closed distinction of exactly two kinds: a project booking and a journey booking. Both kinds SHALL share start time, end time, task, working location, project-relevance, the booked project's ZEP identifier, and the note. A journey booking SHALL additionally carry a journey direction and a vehicle. A project booking SHALL additionally carry its process reference. Every booking SHALL expose its date, derived from the start time, and its duration in hours, derived from the start and end times.

#### Scenario: A project booking exposes date and duration
- **WHEN** a project booking is constructed
- **THEN** it exposes the date derived from its start time and the duration in hours between its start and end times

#### Scenario: A journey booking carries direction and vehicle
- **WHEN** a journey booking is constructed
- **THEN** it carries a journey direction and a vehicle in addition to the shared booking fields

#### Scenario: Both booking kinds carry project and note
- **WHEN** a project booking or a journey booking is constructed
- **THEN** it carries the booked project's ZEP identifier and the booking note

## ADDED Requirements

### Requirement: ZEP attendance project and note map onto the booking
The mapping from a ZEP attendance record to a booking SHALL take the booked project's ZEP identifier from the record's project identifier and the note from the record's note text unchanged. An absent note SHALL map to an absent note on the booking rather than raising a mapping error.

#### Scenario: Project and note are taken from the attendance record
- **WHEN** a ZEP attendance record for project 42 carries the note `Intern - Teammeeting`
- **THEN** the produced booking carries project ZEP identifier 42 and the note `Intern - Teammeeting`

#### Scenario: A record without a note still produces a booking
- **WHEN** a ZEP attendance record carries no note
- **THEN** a booking is produced with an absent note
