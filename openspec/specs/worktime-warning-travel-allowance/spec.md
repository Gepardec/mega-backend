# worktime-warning-travel-allowance Specification

## Purpose
Defines how much travel allowance ("Reisepauschale") a day's passive journeys permit, and when travel-allowance bookings are flagged because they exceed that allowance (`TRAVEL_ALLOWANCE_EXCEEDED`) or were booked on "Intern" for a customer trip (`TRAVEL_ALLOWANCE_WRONG_PROJECT`).

## Requirements

### Requirement: Passive journeys permit a travel allowance per journey
Every passive journey SHALL permit a travel allowance on its date: 15 minutes on a short route, 30 minutes otherwise. A journey is passive when its vehicle is not driven by the employee (e.g. train, bus, passenger in a car). Active journeys SHALL permit no allowance. Outbound, onward and return journeys SHALL each count, and the allowances of a day's journeys SHALL add up.

#### Scenario: A day trip by train permits two allowances
- **WHEN** an employee travels by train from Wien to Linz and back on the same day
- **THEN** the day permits a travel allowance of 60 minutes

#### Scenario: An active journey permits no allowance
- **WHEN** an employee travels by train from Wien to Linz and drives back by car on the same day
- **THEN** the day permits a travel allowance of 30 minutes

#### Scenario: A multi-day trip permits allowances on its travel days
- **WHEN** an employee travels by train from Wien to Linz on one day and back two days later
- **THEN** each of the two travel days permits a travel allowance of 30 minutes and the day in between permits none

#### Scenario: An onward journey permits its own allowance
- **WHEN** an employee travels by train from Wien to Linz and onward to St. Pölten on the same day
- **THEN** the day permits a travel allowance of 45 minutes

### Requirement: Routes between St. Pölten and Wien or Linz are short routes
A journey between St. Pölten and Wien, or between St. Pölten and Linz, SHALL be a short route in either direction. Every other route SHALL be an ordinary route, including routes whose start or destination is not recognised, is missing, or is the same place on both ends.

#### Scenario: St. Pölten to Wien is a short route
- **WHEN** a passive journey goes from St. Pölten to Wien
- **THEN** it permits a travel allowance of 15 minutes

#### Scenario: Short routes apply in both directions
- **WHEN** a passive journey goes from Linz to St. Pölten
- **THEN** it permits a travel allowance of 15 minutes

#### Scenario: St. Pölten to Salzburg is an ordinary route
- **WHEN** a passive journey goes from St. Pölten to Salzburg
- **THEN** it permits a travel allowance of 30 minutes

#### Scenario: An unrecognised place yields an ordinary route
- **WHEN** a passive journey goes from St. Pölten to a destination that is not recognised as a known place
- **THEN** it permits a travel allowance of 30 minutes

#### Scenario: A missing start yields an ordinary route
- **WHEN** a passive journey has no start
- **THEN** it permits a travel allowance of 30 minutes

### Requirement: Known places are recognised despite spelling variants
Start and destination are free text. The system SHALL recognise Wien, Linz and St. Pölten regardless of letter case, punctuation, a "Sankt" or "St." prefix, an umlaut spelled as "oe", and additional words such as a station or district. A text that merely contains a place name as part of a longer word, or that names more than one known place, SHALL NOT be recognised.

#### Scenario: Station names are recognised
- **WHEN** a passive journey goes from "Wien Hbf" to "St. Pölten Hbf"
- **THEN** it permits a travel allowance of 15 minutes

#### Scenario: Spelling variants of St. Pölten are recognised
- **WHEN** passive journeys go from "Sankt Pölten", "St.Pölten" or "st poelten" to "Linz"
- **THEN** each permits a travel allowance of 15 minutes

#### Scenario: A longer word containing a place name is not recognised
- **WHEN** a passive journey goes from "Wiener Neustadt" to "St. Pölten"
- **THEN** it permits a travel allowance of 30 minutes

#### Scenario: A text naming two known places is not recognised
- **WHEN** a passive journey goes from "Linz" to "St. Pölten / Wien"
- **THEN** it permits a travel allowance of 30 minutes

### Requirement: Travel-allowance bookings are bookings on the process "Reisepauschale"
A booking SHALL be a travel-allowance booking when it is booked on the ZEP process "Reisepauschale" of the project "Intern" (process id 873). The travel allowance booked on a day SHALL be the total duration of that day's travel-allowance bookings. Bookings on other processes, including travel allowance booked on customer projects, SHALL NOT count.

#### Scenario: Several travel-allowance bookings on a day add up
- **WHEN** a day carries two bookings of 15 minutes each on the process "Reisepauschale"
- **THEN** the travel allowance booked on that day is 30 minutes

#### Scenario: Bookings on other processes do not count
- **WHEN** a day carries a non-billable booking of 30 minutes on a customer project and no booking on the process "Reisepauschale"
- **THEN** the travel allowance booked on that day is 0 minutes and no travel-allowance warning is produced

### Requirement: Journeys on billable projects are customer trips
A journey SHALL be a customer trip when its project is billable. A journey on a non-billable project, on a project unknown to the system, or without project SHALL be an internal trip. The project's billability SHALL be read as it is when the warnings are requested.

#### Scenario: A journey on "Intern" is an internal trip
- **WHEN** a passive journey is booked on the non-billable project "Intern"
- **THEN** it is an internal trip

#### Scenario: A journey on a billable project is a customer trip
- **WHEN** a passive journey is booked on a project whose billability preset is billable
- **THEN** it is a customer trip

#### Scenario: A journey on an unknown project is an internal trip
- **WHEN** a passive journey refers to a ZEP project that the system does not know
- **THEN** it is an internal trip and the warnings request does not fail

### Requirement: Travel allowance exceeding the internal trips' allowance is reported per day
The system SHALL produce a `TRAVEL_ALLOWANCE_EXCEEDED` warning for every date on which the travel allowance booked exceeds the allowance permitted by that day's passive internal trips. A day without passive internal trips SHALL permit an allowance of 0. The warning SHALL carry the excess in hours, rounded to two decimals.

#### Scenario: Travel allowance within the allowance is not reported
- **WHEN** a day with a train journey from Wien to Linz and back on "Intern" carries 60 minutes on the process "Reisepauschale"
- **THEN** no `TRAVEL_ALLOWANCE_EXCEEDED` warning is produced for that day

#### Scenario: Travel allowance above the allowance is reported with the excess
- **WHEN** a day with a train journey from St. Pölten to Wien on "Intern" carries 30 minutes on the process "Reisepauschale"
- **THEN** a `TRAVEL_ALLOWANCE_EXCEEDED` warning for that day with `hours` 0.25 is produced

#### Scenario: Travel allowance without any journey is reported
- **WHEN** a day without journeys carries 30 minutes on the process "Reisepauschale"
- **THEN** a `TRAVEL_ALLOWANCE_EXCEEDED` warning for that day with `hours` 0.5 is produced

#### Scenario: Travel allowance on a day with only active journeys is reported
- **WHEN** a day whose only journeys are driven by the employee carries 30 minutes on the process "Reisepauschale"
- **THEN** a `TRAVEL_ALLOWANCE_EXCEEDED` warning for that day with `hours` 0.5 is produced

#### Scenario: Customer trips do not add to the internal allowance
- **WHEN** a day with a passive internal trip from Wien to Linz and a passive customer trip back carries 60 minutes on the process "Reisepauschale"
- **THEN** a `TRAVEL_ALLOWANCE_EXCEEDED` warning for that day with `hours` 0.5 is produced

### Requirement: Travel allowance on "Intern" for a customer trip is reported per day
The system SHALL produce a `TRAVEL_ALLOWANCE_WRONG_PROJECT` warning for every date that carries travel allowance and on which every passive journey is a customer trip, with at least one such journey. On such a day the system SHALL NOT also produce `TRAVEL_ALLOWANCE_EXCEEDED`. The warning SHALL carry the date and no hours.

#### Scenario: Travel allowance on "Intern" for a customer trip is reported
- **WHEN** a day with a passive journey on a billable project carries 30 minutes on the process "Reisepauschale"
- **THEN** a `TRAVEL_ALLOWANCE_WRONG_PROJECT` warning for that day without hours is produced and no `TRAVEL_ALLOWANCE_EXCEEDED` warning for that day

#### Scenario: A day with an internal trip is not reported as wrong project
- **WHEN** a day with one passive internal trip and one passive customer trip carries 30 minutes on the process "Reisepauschale"
- **THEN** no `TRAVEL_ALLOWANCE_WRONG_PROJECT` warning is produced for that day

#### Scenario: A customer trip without travel allowance on "Intern" is not reported
- **WHEN** a day with a passive journey on a billable project carries no booking on the process "Reisepauschale"
- **THEN** no `TRAVEL_ALLOWANCE_WRONG_PROJECT` warning is produced for that day

### Requirement: Travel allowance is only checked from the rule's configured start date
Both travel-allowance checks SHALL form the `TRAVEL_ALLOWANCE` warning rule and SHALL be governed by its rule setting. When the setting has an `activeFrom` date, days before it SHALL trigger neither `TRAVEL_ALLOWANCE_EXCEEDED` nor `TRAVEL_ALLOWANCE_WRONG_PROJECT`. The initial `activeFrom` is 2026-10-01.

#### Scenario: A day before the configured start date is not checked
- **WHEN** the `TRAVEL_ALLOWANCE` rule has `activeFrom` 2026-10-01 and 2026-09-30 carries 30 minutes on the process "Reisepauschale" and no journey
- **THEN** no travel-allowance warning is produced for 2026-09-30

#### Scenario: A day on the configured start date is checked
- **WHEN** the `TRAVEL_ALLOWANCE` rule has `activeFrom` 2026-10-01 and 2026-10-01 carries 30 minutes on the process "Reisepauschale" and no journey
- **THEN** a `TRAVEL_ALLOWANCE_EXCEEDED` warning for 2026-10-01 is produced

#### Scenario: A disabled rule checks no days
- **WHEN** the `TRAVEL_ALLOWANCE` rule is disabled and 2026-10-01 carries 30 minutes on the process "Reisepauschale" and no journey
- **THEN** no travel-allowance warning is produced
