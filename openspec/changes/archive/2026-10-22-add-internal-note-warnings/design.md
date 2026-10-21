# Design

## Context

Motivation: see proposal.md, section "Why". Requirements: see `specs/worktime-warning-internal-note/spec.md`.

Current state in the worktime BC:

- The ZEP REST response for attendances contains `billable` and `note` for each booking. The DTO `ZepAttendance` already reads `billable`, but not `note`. Unknown fields are ignored.
- `WorkTimeBookingZepMapper` maps to `ProjectBooking` or `JourneyBooking` (sealed `WorkTimeBooking`). It runs with `unmappedTargetPolicy = ERROR`, so new target fields must be mapped explicitly.
- Warning rules are pure `WorkTimeWarningCalculator` implementations. `WorkTimeWarningAssembler` instantiates them in a fixed list and deduplicates by type and date.
- The REST mapper outputs `WorkTimeWarningType.name()`. In the OpenAPI spec, `type` is a free string without an enum, so the contract does not change.
- Warning texts live in the frontend. The `warning.*` entries in the backend `messages*.properties` are not used for worktime warnings.

## Goals / Non-Goals

**Goals:**
- Make billability and note available as shared fields of both booking kinds.
- Implement both rules of the convention in a single pure domain rule that can be tested in isolation.

**Non-Goals:**
- No frontend translations in this repository.
- No entries in the backend `messages*.properties`.
- No configurable cutoff date.
- No change to the aggregated work time report. It keeps using `billable` through its own attendance model.

## Decisions

### 1. One calculator for both rules

`InternalNoteConventionCalculator` produces both `INTERNAL_NOTE_BILLABLE` and `INTERNAL_NOTE_MISSING`. Both rules check the same convention: same marker detection, same cutoff date. They differ only in the sign of the billability.

- *Alternative:* two calculators, one per type. The other calculators are indeed cut per type, but then two classes would have to share the same marker detection and the same cutoff date and keep them consistent with each other. A calculator that produces several types is already allowed (`InvalidJourneyCalculator` produces both `BACK_MISSING` and `TO_MISSING` via `JourneyDirectionScanner`).

### 2. Marker detection lives with the rule, not in the booking model

The booking carries the note as raw text (`String note`, may be `null`). The calculator decides whether a note is marked as internal: pattern `^\s*intern\s*[-–]` with `CASE_INSENSITIVE` and `find()`, so that only the start counts.

- *Alternative:* a value object `BookingNote` with `isMarkedInternal()`. That would be more domain-oriented, but it pulls the convention into the booking model. `worktime-booking-collection` explicitly states that the booking model makes no warning decisions. If the marker is needed outside of warnings later, detection can be moved into a value object without a spec change.

### 3. Billability as `boolean`, default in the adapter

The new shared field `billable` is a primitive `boolean`. If the ZEP value is missing (`null`), the adapter maps it to `false`. This rule already applies in the report adapter (`Boolean.TRUE.equals(attendance.billable())`). This way the domain model does not need to know a three-valued state.

### 4. Cutoff date as a domain constant

`LocalDate.of(2026, 10, 1)` is a constant of the calculator. It is compared against the booking date (`booking.date()`), i.e. the start date. The cutoff date is a one-time business decision and does not differ between environments. As a constant, the rule stays a pure function without the application layer having to pass configuration through.

- *Alternative:* a config property passed through the service into the assembler. That adds plumbing for a value that is expected never to change. If it does, the refactoring is local.

### 5. `note` in the ZEP DTO

`ZepAttendance` gets the field `note` (`@JsonProperty("note")`) together with a builder method. The DTO lives in the legacy package `zep.rest.dto` and is shared with the legacy code. The new field is additive; existing users are not affected.

## Risks / Trade-offs

- [Existing tests create bookings with Instancio. The new fields `billable` and `note` would then be filled randomly, and tests across the whole assembler could see unexpected warnings.] → If the test data lies before the cutoff date, the rule does not apply. Even so, `WarningTestBookingBuilder` and direct Instancio calls for bookings set the new fields explicitly, and assembler and service tests assert exact warning lists.
- [`INTERNAL_NOTE_MISSING` will fire very often from October 2026 until the convention is lived.] → Intended from a business perspective. Communication to employees and frontend texts are outside this change.
- [The frontend only knows the new types after its own release.] → The backend delivers the types anyway. How the frontend displays unknown types must be agreed before deployment (see Migration Plan).
- [The tolerant pattern does not cover every typing variant (e.g. "Intern:", non-breaking space).] → Deliberately agreed this way. The pattern can be extended locally.

## Migration Plan

- No data migration; the warnings are recalculated on every request.
- Roll out the frontend translation keys for `INTERNAL_NOTE_BILLABLE` and `INTERNAL_NOTE_MISSING` at the same time as or before the backend.
- Rollback: revert the backend release; no persisted follow-up data is created.
