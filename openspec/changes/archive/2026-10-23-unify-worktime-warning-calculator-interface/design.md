# Design

## Context

See proposal.md for the motivation. The current state:

- `WorkTimeWarningCalculator.calculate(WorkTimeBookings)` is implemented by twelve calculators, which the assembler runs as one list.
- Four calculators have their own signatures and are held as separate assembler fields:
  - `NoEntryWarningCalculator(expectedWorkingDays, bookedDates, excusedDates, today)`
  - `NonBillableOnBillableProjectCalculator(bookings, billableProjectZepIds)`
  - `TravelAllowanceCalculator(bookings, billableProjectZepIds)`
  - `MealEntryCalculator(bookings, month, mealEntryDates, today)`
- The assembler runs the list first and then the four in that order, deduplicates per type and date (keeping the first), and returns a single `EMPTY_ENTRY_LIST` warning when there are no bookings.
- `GetEmployeeWarningsService` fetches the extra data from its ports. It skips those calls when the month has no bookings, and skips the meal entry call when there are no journeys.
- The assembler and the calculators are plain classes built once per service (not CDI beans), so request-specific data cannot go into constructors (see #800, design decision 1).

## Goals / Non-Goals

**Goals:**
- One calculator interface and one assembler entry point that do not change when a rule needs new input.
- Keep the twelve bookings-only calculators and their tests untouched apart from the `implements` clause.
- Keep the produced warnings, their order and the deduplication identical.

**Non-Goals:**
- No shared rule start date. Each rule keeps its own `RULE_START`.
- No change to which ports the service calls or when it calls them.
- No CDI-managed calculators or discovery of calculators by injection.

## Decisions

### 1. `WorkTimeEvaluation` is a domain value object in `domain/model`

A record next to `WorkTimeBookings` and `WorkTimeReport`:

```
WorkTimeEvaluation(
    WorkTimeBookings bookings,
    YearMonth payrollMonth,
    LocalDate evaluationDate,
    Set<LocalDate> expectedWorkingDays,
    Set<LocalDate> excusedDates,
    Set<Integer> billableProjectZepIds,
    Set<LocalDate> mealEntryDates)
```

The compact constructor rejects `null` and copies the sets into unmodifiable sets. The name follows the specs, which describe each rule as evaluating bookings. It also gives the current `today` argument a domain name: the evaluation date. `payrollMonth` matches `WorkTimeReport`.

- *Alternatives considered:* `EmployeePayrollMonth`, `WorkTimeMonth` and `WorkTimeWarningContext`. The two month names don't fit the evaluation date. The context name is technical vocabulary rather than domain language. `…Snapshot`, `…Facts` and `…Check` were rejected because they clash with existing terms: cross-BC snapshot ports, the warning as a "fact", and month-end time checks.
- *Alternative:* keep it in `domain/services/warning`. It is a pure value without behaviour that the application layer builds, so `domain/model` is the natural home.

### 2. Two interface levels with a default method

```
interface WorkTimeWarningCalculator {
    List<WorkTimeWarning> calculate(WorkTimeEvaluation evaluation);
}

interface BookingsWarningCalculator extends WorkTimeWarningCalculator {
    List<WorkTimeWarning> calculate(WorkTimeBookings bookings);

    @Override
    default List<WorkTimeWarning> calculate(WorkTimeEvaluation evaluation) {
        return calculate(evaluation.bookings());
    }
}
```

The twelve bookings-only calculators implement `BookingsWarningCalculator`. The four others implement `WorkTimeWarningCalculator` and replace their current signature with `calculate(WorkTimeEvaluation)`. `NoEntryWarningCalculator` derives the booked dates from `evaluation.bookings().bookedDates()` instead of receiving them.

- *Alternative:* every calculator takes the evaluation. That is more uniform, but it touches all twelve simple calculators and their tests and gives rules like `WeekendCalculator` inputs they never use.
- *Alternative:* keep the current special-case wiring. Rejected; it is what this change removes.

### 3. The assembler holds one list in a fixed order

`WorkTimeWarningAssembler` keeps a single `List<WorkTimeWarningCalculator>`. The default constructor lists the twelve current calculators in their current order, followed by `NoEntryWarningCalculator`, `NonBillableOnBillableProjectCalculator`, `TravelAllowanceCalculator` and `MealEntryCalculator`. Because this is the order the assembler calls them in today, the output order and the "first duplicate wins" outcome stay the same. The package-private test constructor becomes `WorkTimeWarningAssembler(List<WorkTimeWarningCalculator>)`.

`assemble(WorkTimeEvaluation)` keeps the early `EMPTY_ENTRY_LIST` return when `evaluation.bookings()` is empty.

### 4. The service passes empty inputs when there are no bookings

When the month has no bookings, `GetEmployeeWarningsService` still skips the absence, expected-working-day, project and meal entry calls. It builds a `WorkTimeEvaluation` with empty sets and passes it to the assembler, which returns the month-level warning. The meal entry call is still skipped when there are no journeys. This keeps the current port-call behaviour.

- *Alternative:* a static factory such as `WorkTimeEvaluation.bookingsOnly(...)` for the empty case. It saves a few `Set.of()` arguments in one place and adds API surface. Not worth it for a single call site.

### 5. Tests build evaluations with Instancio

The tests for the four outlier calculators and the assembler build `WorkTimeEvaluation` with `Instancio.of(WorkTimeEvaluation.class).set(field(...), ...)`, setting only the fields the test is about. Mocked calculators in `WorkTimeWarningAssemblerTest` stub `calculate(same(evaluation))`.

Instancio fills unset sets with random values, so a test that relies on an input being empty has to set that field explicitly. Each test sets every input its rule reads.

## Risks / Trade-offs

- [A rule can read any evaluation field, including ones that have nothing to do with it] → Accepted. Rules that only need bookings are kept away from the evaluation by `BookingsWarningCalculator`. The rest read what they need, and their tests show which fields matter.
- [Moving the four outliers into the shared list could change the warning order or which duplicate is kept] → They are appended in the order the assembler calls them today. The existing assembler tests for ordering and deduplication cover this and must pass unchanged in what they assert.
- [`WorkTimeEvaluation` grows a field with every new kind of input] → Intended. That one field replaces a change to the assembler signature, its constructor, the service call and every assembler test.
