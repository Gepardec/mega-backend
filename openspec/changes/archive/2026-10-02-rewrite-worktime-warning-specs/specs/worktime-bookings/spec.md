# Spec Delta

## MODIFIED Requirements

### Requirement: ZEP attendance fields map to the booking vocabulary
The mapping from a ZEP attendance record to a booking SHALL apply these rules: the activity maps to a task, and a journey task selects the journey booking, otherwise the project booking; the direction of travel maps to the journey direction, defaulting to the outbound direction when absent; the vehicle identifier maps to a vehicle; the work-location code maps to a working location, defaulting to the main location when absent. A record whose end time is 00:00 (ZEP's representation of 24:00) SHALL end at 00:00 of the following day; the booking still belongs to the date it starts on. A record whose activity or vehicle cannot be mapped to a known vocabulary value SHALL raise a mapping error rather than produce a booking. The task, working location, journey direction, and vehicle vocabularies SHALL be owned by the worktime bounded context (the mapping from raw ZEP codes lives in the outbound adapter).

#### Scenario: Known codes map to the correct vocabulary values
- **WHEN** a ZEP attendance record carries a known activity, vehicle, and work-location code
- **THEN** the produced booking carries the corresponding task, vehicle, and working location

#### Scenario: Missing optional codes fall back to defaults
- **WHEN** a journey attendance record has no direction of travel, or a record has no work-location code
- **THEN** the journey direction defaults to outbound and the working location defaults to the main location

#### Scenario: A record ending at 24:00 ends at the start of the following day
- **WHEN** a ZEP attendance record on a date runs from 18:00 to 00:00
- **THEN** the produced booking ends at 00:00 of the following day, belongs to the record's date, and has a duration of 6 hours

#### Scenario: An unmappable required value raises a mapping error
- **WHEN** a ZEP attendance record carries an activity or vehicle that is not part of the worktime vocabulary
- **THEN** a mapping error is raised and no booking is produced for that record
