# Spec Delta

## MODIFIED Requirements

### Requirement: Bookings are modelled as a project-or-journey distinction
The detailed booking model SHALL be a closed distinction of exactly two kinds: a project booking and a journey booking. Both kinds SHALL share start time, end time, task, working location, project-relevance, billability, note, and a reference to the ZEP project the booking was made on. The project reference is absent when ZEP supplies none. A journey booking SHALL additionally carry a journey direction and a vehicle. A project booking SHALL additionally carry its process reference. Every booking SHALL expose its date, derived from the start time, and its duration in hours, derived from the start and end times.

#### Scenario: A project booking exposes date and duration
- **WHEN** a project booking is constructed
- **THEN** it exposes the date derived from its start time and the duration in hours between its start and end times

#### Scenario: A journey booking carries direction and vehicle
- **WHEN** a journey booking is constructed
- **THEN** it carries a journey direction and a vehicle in addition to the shared booking fields

#### Scenario: Both booking kinds carry billability and note
- **WHEN** a project booking or a journey booking is constructed
- **THEN** it carries whether it is billable and its note text

#### Scenario: Both booking kinds carry their project reference
- **WHEN** a project booking or a journey booking is constructed
- **THEN** it carries the ZEP project reference it was booked on, or no project reference

### Requirement: ZEP attendance fields map to the booking vocabulary
The mapping from a ZEP attendance record to a booking SHALL apply these rules: the activity maps to a task, and a journey task selects the journey booking, otherwise the project booking; the direction of travel maps to the journey direction, defaulting to the outbound direction when absent; the vehicle identifier maps to a vehicle; the work-location code maps to a working location, defaulting to the main location when absent; the billable flag maps to the booking's billability, defaulting to not billable when absent; the note maps to the booking's note, which is absent when ZEP supplies none; the project id maps to the booking's project reference, which is absent when ZEP supplies none. A record whose end time is 00:00 (ZEP's representation of 24:00) SHALL end at 00:00 of the following day; the booking still belongs to the date it starts on. A record whose activity or vehicle cannot be mapped to a known vocabulary value SHALL raise a mapping error rather than produce a booking. The task, working location, journey direction, and vehicle vocabularies SHALL be owned by the worktime bounded context (the mapping from raw ZEP codes lives in the outbound adapter).

#### Scenario: Known codes map to the correct vocabulary values
- **WHEN** a ZEP attendance record carries a known activity, vehicle, and work-location code
- **THEN** the produced booking carries the corresponding task, vehicle, and working location

#### Scenario: Missing optional codes fall back to defaults
- **WHEN** a journey attendance record has no direction of travel, or a record has no work-location code
- **THEN** the journey direction defaults to outbound and the working location defaults to the main location

#### Scenario: Billable flag and note are carried over
- **WHEN** a ZEP attendance record is billable and carries the note "Intern - JFX"
- **THEN** the produced booking is billable and carries the note "Intern - JFX"

#### Scenario: A missing billable flag means not billable
- **WHEN** a ZEP attendance record carries no billable flag
- **THEN** the produced booking is not billable

#### Scenario: A missing note yields a booking without note
- **WHEN** a ZEP attendance record carries no note
- **THEN** the produced booking carries no note

#### Scenario: The project id is carried over as project reference
- **WHEN** a ZEP attendance record carries the project id 42
- **THEN** the produced booking refers to the ZEP project 42

#### Scenario: A missing project id yields a booking without project reference
- **WHEN** a ZEP attendance record carries no project id
- **THEN** the produced booking carries no project reference

#### Scenario: A record ending at 24:00 ends at the start of the following day
- **WHEN** a ZEP attendance record on a date runs from 18:00 to 00:00
- **THEN** the produced booking ends at 00:00 of the following day, belongs to the record's date, and has a duration of 6 hours

#### Scenario: An unmappable required value raises a mapping error
- **WHEN** a ZEP attendance record carries an activity or vehicle that is not part of the worktime vocabulary
- **THEN** a mapping error is raised and no booking is produced for that record
