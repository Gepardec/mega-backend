# Spec Delta

## Purpose

Defines when bookings on a public holiday are flagged with `HOLIDAY`.

## ADDED Requirements

### Requirement: Bookings on public holidays are flagged
The system SHALL produce a `HOLIDAY` warning for every date that carries at least one booking of any kind and is a public holiday according to the office calendar (Austria). The warning SHALL carry the date and no hours.

#### Scenario: A booking on a public holiday is flagged
- **WHEN** an employee has a booking on a public holiday
- **THEN** a `HOLIDAY` warning is produced for that date

#### Scenario: A public holiday without bookings is not flagged
- **WHEN** a public holiday carries no booking
- **THEN** no `HOLIDAY` warning is produced for that date
