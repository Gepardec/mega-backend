# Spec Delta

## Purpose

Defines the permitted time windows for doctor's appointments and when a doctor's-appointment booking is flagged with `WRONG_DOCTOR_APPOINTMENT`.

## ADDED Requirements

### Requirement: Doctor's appointments must lie entirely within a permitted window
The system SHALL produce a `WRONG_DOCTOR_APPOINTMENT` warning for the date of every project booking on the doctor's-appointment process that does not lie entirely within one of the permitted windows 08:30–12:00 or 12:30–17:00 on its start date. Window boundaries SHALL be inclusive. A booking ending at 24:00 SHALL be treated as ending after 17:00. The warning SHALL carry the date and no hours. Bookings on other processes SHALL NOT be checked.

#### Scenario: An appointment within the morning window is permitted
- **WHEN** a doctor's-appointment booking runs from 08:30 to 12:00
- **THEN** no `WRONG_DOCTOR_APPOINTMENT` warning is produced

#### Scenario: An appointment within the afternoon window is permitted
- **WHEN** a doctor's-appointment booking runs from 12:30 to 17:00
- **THEN** no `WRONG_DOCTOR_APPOINTMENT` warning is produced

#### Scenario: An appointment starting before 08:30 is flagged
- **WHEN** a doctor's-appointment booking runs from 08:00 to 09:00
- **THEN** a `WRONG_DOCTOR_APPOINTMENT` warning is produced for that date

#### Scenario: An appointment within the lunch window is flagged
- **WHEN** a doctor's-appointment booking runs from 12:00 to 12:30
- **THEN** a `WRONG_DOCTOR_APPOINTMENT` warning is produced for that date

#### Scenario: An appointment spanning the lunch window is flagged
- **WHEN** a doctor's-appointment booking runs from 11:00 to 12:30
- **THEN** a `WRONG_DOCTOR_APPOINTMENT` warning is produced for that date

#### Scenario: An appointment ending after 17:00 is flagged
- **WHEN** a doctor's-appointment booking runs from 16:00 to 17:30
- **THEN** a `WRONG_DOCTOR_APPOINTMENT` warning is produced for that date

#### Scenario: An appointment ending at 24:00 is flagged
- **WHEN** a doctor's-appointment booking runs from 18:00 to 24:00
- **THEN** a `WRONG_DOCTOR_APPOINTMENT` warning is produced for the booking's start date

#### Scenario: Other processes are not checked
- **WHEN** a project booking on any other process runs from 18:00 to 20:00
- **THEN** no `WRONG_DOCTOR_APPOINTMENT` warning is produced
