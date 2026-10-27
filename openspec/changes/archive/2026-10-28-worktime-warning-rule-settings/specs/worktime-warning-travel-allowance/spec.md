# Spec Delta

## ADDED Requirements

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

## REMOVED Requirements

### Requirement: Travel allowance is only checked from 2026-10-01 onward
**Reason**: The fixed cutoff date is replaced by the rule's configurable start date.
**Migration**: The rule's initial setting is `activeFrom` 2026-10-01, so behaviour is unchanged until an administrator changes it.
