## Why

Warning calculators repeatedly sort, filter, group, and aggregate raw `List<WorkTimeBooking>` inputs, leaving collection invariants duplicated across domain services and forcing three calculators to inherit utility behaviour from `AbstractTimeWarningCalculationStrategy`. A dedicated immutable collection value object can establish one canonical ordering, express recurring booking subsets in domain language, and let every calculator focus only on its warning rule.

## What Changes

- Introduce `WorkTimeBookings`, an immutable worktime-domain collection value object that defensively copies bookings and maintains canonical chronological order by start and end time.
- Give the value object domain-specific operations for per-date grouping, working-time-contributing bookings, project bookings, journey bookings, booked dates, emptiness, traversal, and total duration.
- Change `WorkTimeWarningCalculator` and the warning assembler to pass `WorkTimeBookings` instead of raw `List<WorkTimeBooking>`, constructing the value object once at the domain boundary.
- Refactor all booking-based warning calculators to use the value object's canonical views while preserving their warning decisions, multiplicity, and quantities; booking-derived traversal becomes deterministically chronological instead of depending on provider order.
- Remove `AbstractTimeWarningCalculationStrategy` after its grouping and duration responsibilities move into `WorkTimeBookings`.
- Add focused value-object tests and retain the migrated calculator parity suites as regression coverage.

## Capabilities

### New Capabilities
- `worktime-booking-collection`: Immutable collection semantics, canonical chronological ordering, and domain-specific views and aggregations for detailed worktime bookings.

### Modified Capabilities

None. Warning content and REST contracts remain unchanged; response ordering has no existing contractual guarantee.

## Impact

- **Domain model:** adds `WorkTimeBookings` under `com.gepardec.mega.hexagon.worktime.domain.model`.
- **Domain services:** changes the internal calculator input contract and refactors all booking-based warning calculators and `WorkTimeWarningAssembler` to consume the value object.
- **Removed:** `AbstractTimeWarningCalculationStrategy`.
- **Tests:** adds value-object unit tests and adapts calculator, assembler, and application-service tests without reducing the existing parity matrix.
- **External contracts:** no REST, OpenAPI, persistence, ZEP integration, or frontend changes.
- **Dependencies:** no new production dependency.
