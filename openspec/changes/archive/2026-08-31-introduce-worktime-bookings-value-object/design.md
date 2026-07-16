## Context

The worktime warning domain currently represents a month's detailed bookings as `List<WorkTimeBooking>`. Six temporal calculators independently sort that list, three calculators inherit grouping and duration helpers from `AbstractTimeWarningCalculationStrategy`, and other calculators traverse provider order directly. The repeated transformations obscure the shared invariant that warning rules operate on immutable, chronologically ordered bookings.

`WorkTimeBooking` is already a sealed project-or-journey domain model. This change introduces a collection value object around that model; it does not change booking mapping, warning algorithms, warning types, or external interfaces.

## Goals / Non-Goals

**Goals:**

- Establish immutable chronological ordering once when detailed bookings enter the domain calculation flow.
- Express recurring collection operations with worktime-domain names.
- Pass the same `WorkTimeBookings` instance to every booking-based warning calculator.
- Remove `AbstractTimeWarningCalculationStrategy` without moving warning decisions into the value object.
- Preserve warning types, dates, quantities, and multiplicity, guarded by the per-calculator suites.
- Make booking-derived traversal deterministic when the provider returns unordered input.

**Non-Goals:**

- Change warning thresholds, warning algorithms, or warning aggregation order across calculator types.
- Move holiday, overlap, break, rest, journey-validation, or other compliance decisions into the collection value object.
- Change the ZEP outbound port, REST/OpenAPI contract, persistence, or frontend behaviour.
- Generalise the value object into a predicate-based collection or stream utility.
- Refactor the aggregate `WorkTimeAttendance` reporting model.

## Decisions

### 1. Model an immutable `WorkTimeBookings` collection value object

Add `WorkTimeBookings` to `worktime.domain.model` as a value type containing an unmodifiable `List<WorkTimeBooking>`. Construction SHALL reject a null collection or null elements, defensively copy the input, and apply a stable comparator by `from` and then `to`.

The record/class accessor may expose the immutable list, and the type may implement `Iterable<WorkTimeBooking>`, but mutation APIs are prohibited. Structural equality is appropriate because the type has no identity beyond its ordered booking values.

Alternative considered: sort in `WorkTimeWarningAssembler` and continue passing `List`. This removes repeated sorting but leaves raw collection semantics and helper duplication spread across calculators, so it does not create a domain abstraction.

### 2. Expose domain-specific collection operations

The initial API will cover only operations already repeated or naturally owned by the collection:

- `isEmpty()` and chronological traversal/access;
- `byDate()` returning an insertion-ordered, unmodifiable `Map<LocalDate, WorkTimeBookings>`;
- `contributingToWorkingTime()` containing project/task bookings plus journeys whose vehicle marks an active traveller;
- `projects()` and `journeys()` as typed, chronologically ordered immutable views;
- `bookedDates()` as an encounter-ordered immutable set;
- `totalDuration()` returning `Duration` rather than an accumulated floating-point hour value.

Calculators convert `Duration` into their existing hour representation only where a warning quantity requires it.

Alternative considered: provide `filter(Predicate)` and `groupByDate(List<Predicate>)`. Those methods would reproduce the procedural superclass behind a collection facade and would allow arbitrary rule logic to leak into the value object. Named domain operations make the shared concepts explicit and keep warning-specific predicates in calculators.

### 3. Change the internal calculator contract

`WorkTimeWarningCalculator.calculate` will accept `WorkTimeBookings`. `GetEmployeeWarningsService` will wrap the outbound port's list immediately after fetching, and `WorkTimeWarningAssembler` will receive and pass that same value object to every calculator. It will use `isEmpty()` and `bookedDates()` for its own orchestration.

`NoEntryWarningCalculator` remains separate because it calculates a set difference over dates rather than detailed bookings.

Alternative considered: let every calculator construct `WorkTimeBookings` from its list argument. That retains compatibility at the cost of repeated sorting and fails to establish the invariant at the domain boundary.

### 4. Remove the abstract calculation strategy

The three inheriting calculators will use `byDate()` and `totalDuration()` directly. Once no subclasses remain, delete `AbstractTimeWarningCalculationStrategy`. Calculators remain stateless domain services implementing the same interface; composition replaces inheritance.

### 5. Preserve rule ownership, and guard behaviour by ordering-independence

The value object owns collection facts only. Thresholds, allowed time windows, overlap definitions, break/rest calculations, holiday knowledge, and journey state transitions remain in their respective calculators. Existing per-calculator suites stay intact and are adapted to create `WorkTimeBookings`; new tests target value-object invariants and views.

For calculators that previously traversed raw provider order, traversal becomes chronological. This is deterministic and consistent with the temporal calculators. Warning response ordering is not an existing API guarantee, while warning content and multiplicity remain unchanged.

Behaviour is guarded by three layers that describe the new code on its own terms rather than by comparison against the retired implementation, which is being sunset:

- the per-calculator suites in `domain/services/warning`, which own each rule's thresholds and edge cases;
- the value-object invariant tests, which own ordering, immutability, grouping, views, dates, and duration;
- a cross-calculator ordering-independence test, which feeds every booking-based calculator the same bookings in chronological and in deliberately scrambled order and requires identical warning types, dates, quantities, and occurrence counts.

The third layer is what makes the canonicalisation safe to rely on: it states the property the value object exists to provide, so it stays meaningful once no earlier implementation remains to compare against.

## Risks / Trade-offs

- **[Risk] The collection type becomes a dumping ground for warning rules.** → Limit its API to collection facts and reusable worktime concepts; prohibit warning types, thresholds, calendars, and validation outcomes from the model.
- **[Risk] Canonical sorting changes incidental ordering for scan-only calculators.** → Specify chronological traversal, retain fixed calculator assembly order, and assert warning content/multiplicity plus deterministic date order in tests.
- **[Risk] `byDate()` repeatedly creates nested value objects and sorts already sorted subsets.** → Preserve encounter order and use a trusted private construction path for pre-sorted subsets if profiling shows a need; prefer correctness and immutability initially.
- **[Risk] Returning raw streams reintroduces procedural duplication.** → Prefer typed/domain methods; expose only minimal traversal needed for calculator-specific rules.
- **[Trade-off] Internal calculator signatures change across all tests and call sites.** → Perform the refactor atomically and rely on compile errors plus the warning-domain suite in `domain/services/warning` to enumerate affected consumers.

## Migration Plan

1. Add `WorkTimeBookings` and focused tests for ordering, immutability, grouping, subsets, dates, and duration.
2. Change the calculator interface and warning assembly/application boundary to use the value object.
3. Migrate calculators incrementally to the value-object operations while running their dedicated suites.
4. Delete `AbstractTimeWarningCalculationStrategy` after its final subclass is migrated.
5. Run the warning-domain suite including the ordering-independence test, architecture tests, and the full Maven build.

Rollback is a source-level revert: restore the raw-list calculator signature and superclass. There is no data or deployment migration.

## Open Questions

None. The implementation may choose a record or final class as long as structural equality, immutability, and the specified API semantics are preserved.
