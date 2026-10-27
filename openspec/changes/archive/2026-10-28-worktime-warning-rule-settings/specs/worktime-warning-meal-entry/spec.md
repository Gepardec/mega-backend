# Spec Delta

## ADDED Requirements

### Requirement: Only past trip days from the rule's configured start date are checked
The meal-entry rule SHALL be the `MEAL_ENTRY` warning rule and SHALL be governed by its rule setting. It SHALL only check trip days before the current date. When the setting has an `activeFrom` date, trip days before it SHALL NOT trigger `MEAL_ENTRY_MISSING`. The current date and later days SHALL NOT trigger it. The initial `activeFrom` is 2026-10-01.

#### Scenario: A trip day before the configured start date is not checked
- **WHEN** the `MEAL_ENTRY` rule has `activeFrom` 2026-10-01 and 2026-09-30 is a trip day without a meal entry
- **THEN** no `MEAL_ENTRY_MISSING` warning is produced for 2026-09-30

#### Scenario: A trip day on the configured start date is checked
- **WHEN** the `MEAL_ENTRY` rule has `activeFrom` 2026-10-01, 2026-10-01 is a trip day without a meal entry, and the current date is later
- **THEN** a `MEAL_ENTRY_MISSING` warning for 2026-10-01 is produced

#### Scenario: The current date is not checked
- **WHEN** the current date is a trip day without a meal entry
- **THEN** no `MEAL_ENTRY_MISSING` warning is produced for the current date

#### Scenario: Future trip days are not checked
- **WHEN** a trip open at the end of the month extends past the current date
- **THEN** no `MEAL_ENTRY_MISSING` warning is produced for the days after the current date

#### Scenario: A disabled rule checks no trip days
- **WHEN** the `MEAL_ENTRY` rule is disabled and a past trip day on or after 2026-10-01 has no meal entry
- **THEN** no `MEAL_ENTRY_MISSING` warning is produced

## REMOVED Requirements

### Requirement: Only past trip days from 2026-10-01 onward are checked
**Reason**: The fixed cutoff date is replaced by the rule's configurable start date. The "past trip days only" behaviour is kept in the replacement requirement.
**Migration**: The rule's initial setting is `activeFrom` 2026-10-01, so behaviour is unchanged until an administrator changes it.
