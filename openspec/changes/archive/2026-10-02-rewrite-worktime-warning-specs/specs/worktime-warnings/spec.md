# Spec Delta

## ADDED Requirements

### Requirement: Warning rules run as pure functions
Each warning rule SHALL be defined by its own `worktime-warning-*` capability. Warning rules SHALL NOT read the wall clock, employee master data, or external systems directly — every such input SHALL be supplied by the application layer. The current date, where a rule needs it, SHALL be supplied from an injected clock.

#### Scenario: The current date comes from the injected clock
- **WHEN** the no-time-entry rule excludes future days
- **THEN** it excludes the current date and later days using the current date supplied by the injected clock, not the process wall-clock time

### Requirement: At most one warning per type and date
The assembled result SHALL contain at most one warning per combination of warning type and date, even when a rule finds several offending bookings on the same date.

#### Scenario: Several violations of one rule on a day yield one warning
- **WHEN** a rule finds two offending bookings on the same date
- **THEN** the result contains exactly one warning of that rule's type for that date

## REMOVED Requirements

### Requirement: Calculators preserve legacy behaviour and run as pure functions
**Reason**: The legacy calculators no longer exist, so "same warnings as legacy" is not verifiable and turned legacy bugs into requirements. Each rule is now specified in its own `worktime-warning-*` capability.
**Migration**: The pure-function and injected-clock constraints move to "Warning rules run as pure functions"; rule behaviour is defined by the `worktime-warning-*` capabilities.

### Requirement: No-time-entry warnings are a set difference over expected working days
**Reason**: Moved into its own capability alongside the other warning rules.
**Migration**: See `worktime-warning-no-time-entry`; behaviour is unchanged.
