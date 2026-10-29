# Spec Delta

## ADDED Requirements

### Requirement: Billable-project bookings are only checked from the rule's configured start date
The rule SHALL be the `BILLABLE_PROJECT` warning rule and SHALL be governed by its rule setting. When the setting has an `activeFrom` date, bookings dated before it SHALL NOT trigger `NON_BILLABLE_ON_BILLABLE_PROJECT`. The initial `activeFrom` is 2026-10-01.

#### Scenario: A booking before the configured start date is not checked
- **WHEN** the `BILLABLE_PROJECT` rule has `activeFrom` 2026-10-01 and a non-billable booking on 2026-09-30 refers to a billable project
- **THEN** no `NON_BILLABLE_ON_BILLABLE_PROJECT` warning is produced for 2026-09-30

#### Scenario: A booking on the configured start date is checked
- **WHEN** the `BILLABLE_PROJECT` rule has `activeFrom` 2026-10-01 and a non-billable booking on 2026-10-01 refers to a billable project
- **THEN** a `NON_BILLABLE_ON_BILLABLE_PROJECT` warning is produced for 2026-10-01

#### Scenario: A disabled rule checks no bookings
- **WHEN** the `BILLABLE_PROJECT` rule is disabled and a non-billable booking on 2026-10-01 refers to a billable project
- **THEN** no `NON_BILLABLE_ON_BILLABLE_PROJECT` warning is produced

## REMOVED Requirements

### Requirement: Billable-project bookings are only checked from 2026-10-01 onward
**Reason**: The fixed cutoff date is replaced by the rule's configurable start date.
**Migration**: The rule's initial setting is `activeFrom` 2026-10-01, so behaviour is unchanged until an administrator changes it.
