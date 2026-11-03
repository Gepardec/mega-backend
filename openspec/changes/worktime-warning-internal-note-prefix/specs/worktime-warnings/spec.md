# Spec Delta

## MODIFIED Requirements

### Requirement: The warning-type vocabulary is preserved from the legacy feature
The system SHALL define the warning-type vocabulary covering the time-related types `OUTSIDE_CORE_WORKING_TIME`, `TIME_OVERLAP`, `NO_TIME_ENTRY`, `EMPTY_ENTRY_LIST`, `HOLIDAY`, `WEEKEND`, `WRONG_DOCTOR_APPOINTMENT`, `EXCESS_WORKING_TIME_PRESENT`, `MISSING_REST_TIME`, `MISSING_BREAK_TIME` and the journey-related types `BACK_MISSING`, `TO_MISSING`, `INVALID_WORKING_LOCATION`, `LOCATION_RELEVANT_SET`. These type identifiers SHALL match the legacy identifiers so that existing frontend translation keys continue to resolve unchanged. In addition to the preserved legacy types, the vocabulary SHALL contain the note-convention types `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` and `INTERNAL_PREFIX_MISSING`, which have no legacy counterpart.

#### Scenario: Each calculator emits its preserved warning-type identifier
- **WHEN** a calculator for a rule carried over from the legacy feature produces a warning
- **THEN** the warning's type is one of the preserved identifiers, spelled exactly as in the legacy feature

#### Scenario: The note-convention rule emits its new warning-type identifiers
- **WHEN** the "Intern -" note-convention rule produces a warning
- **THEN** the warning's type is `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` or `INTERNAL_PREFIX_MISSING`
