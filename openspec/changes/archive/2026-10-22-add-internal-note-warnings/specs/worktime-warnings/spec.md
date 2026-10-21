# Spec Delta

## MODIFIED Requirements

### Requirement: The warning-type vocabulary is preserved from the legacy feature
The system SHALL define the warning-type vocabulary covering the time-related types `OUTSIDE_CORE_WORKING_TIME`, `TIME_OVERLAP`, `NO_TIME_ENTRY`, `EMPTY_ENTRY_LIST`, `HOLIDAY`, `WEEKEND`, `WRONG_DOCTOR_APPOINTMENT`, `EXCESS_WORKING_TIME_PRESENT`, `MISSING_REST_TIME`, `MISSING_BREAK_TIME`, the journey-related types `BACK_MISSING`, `TO_MISSING`, `INVALID_WORKING_LOCATION`, `LOCATION_RELEVANT_SET`, and the booking-convention types `INTERNAL_NOTE_BILLABLE`, `INTERNAL_NOTE_MISSING`. The type identifiers that exist in the legacy feature SHALL match the legacy identifiers so that existing frontend translation keys continue to resolve unchanged. The booking-convention types have no legacy counterpart and require new frontend translation keys.

#### Scenario: Each calculator emits its preserved warning-type identifier
- **WHEN** a calculator for a rule that exists in the legacy feature produces a warning
- **THEN** the warning's type is one of the preserved identifiers, spelled exactly as in the legacy feature

#### Scenario: Booking-convention rules emit their own identifiers
- **WHEN** the internal-note rules produce a warning
- **THEN** the warning's type is `INTERNAL_NOTE_BILLABLE` or `INTERNAL_NOTE_MISSING`
