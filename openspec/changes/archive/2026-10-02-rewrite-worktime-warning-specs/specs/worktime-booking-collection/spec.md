# Spec Delta

## ADDED Requirements

### Requirement: Warning calculators consume one canonical booking collection
Every detailed-booking warning calculator SHALL accept `WorkTimeBookings` instead of a raw booking list. The warning application flow SHALL construct the value object once after fetching detailed bookings and SHALL pass that same canonical collection through warning assembly to every booking-based calculator.

#### Scenario: Unordered provider input is canonicalised once
- **WHEN** the outbound provider returns detailed bookings in a non-chronological order
- **THEN** the warning application flow constructs one canonical `WorkTimeBookings` value and all booking-based calculators consume its chronological views

## REMOVED Requirements

### Requirement: Warning calculators consume the canonical collection value object
**Reason**: Its parity clause ("equivalent to the pre-refactoring behaviour") pins warning outcomes to a past implementation instead of the specified rules.
**Migration**: The collection-input part is kept in "Warning calculators consume one canonical booking collection"; warning outcomes are defined by the `worktime-warning-*` capabilities.
