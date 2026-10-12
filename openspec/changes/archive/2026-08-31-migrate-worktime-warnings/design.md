## Context

The warning feature is the last major capability in the legacy backend. It is a self-contained pipeline hanging off one legacy REST call:

```
WorkerResourceImpl.getAllWarningsForEmployeeAndMonth(month)
  ├─ employeeService.getEmployee(zepUsername)          → Employee (master data)
  ├─ zepService.getAbsenceForEmployee(emp, month)      → List<AbsenceTime>
  ├─ zepService.getProjectTimes(emp, month)            → List<ProjectEntry>   (rich per-slot detail)
  └─ TimeWarningServiceImpl → WarningCalculatorsManager
        ├─ determineTimeWarnings(entries)              → 8 time calculators
        ├─ determineNoTimeEntries(emp, entries, abs)   → NoEntryCalculator
        ├─ determineJourneyWarnings(entries)           → 3 journey calculators
        ├─ merge-by-date + dedup
        └─ enum → i18n string via ResourceBundle       (DEAD: never reaches the frontend)
     → group-by-type → List<WorkTimeBookingWarning> → List<WorkTimeBookingWarningDto>
```

**The twelve calculators are the asset to preserve.** Everything around them is accidental complexity:

- The `ResourceBundle` message baking in `WarningCalculatorsManager` produces localized strings that `TimeWarningServiceImpl` then discards — it ships only `type.name()` + date + optional hours. The current frontend already keys off the enum constant name (transloco). So "no messages in the backend" is the de-facto contract today.
- The output models (`TimeWarning`, `JourneyWarning`) are mutable bags with setters and `mergeBreakWarnings`.
- The input model is the `ProjectEntry` interface + `ProjectTimeEntry`/`JourneyTimeEntry` classes with builders and `instanceof` subtype checks.
- `NoEntryCalculator` reaches into employee master data, the office calendar, and `LocalDate.now()` directly.

**Verified enabling facts:**

- `ZepAttendance` (already fetched by the worktime BC's `AttendanceService` via `WorkTimeZepAdapter`) carries every field the calculators need: `from`, `to`, `date`, `activity`, `workLocation`, `workLocationIsProjectRelevant`, `vehicle`, `directionOfTravel`. No new ZEP integration is required — only a richer mapping of the same fetch.
- `OfficeCalendarUtil` already exists in the hexagon at `hexagon/shared/domain/util`.
- The worktime BC already has `Absence(date, type)` (per-day, typed), `WorkTimeAbsenceZepPort`, `WorkTimeUserSnapshotPort`, `AuthenticatedActorContext` self-scoping, `WorkTimeRestTransportHelper.parsePayrollMonth`, and the contract-first OpenAPI setup (`openapi/paths/worktime.yaml`, `openapi/schemas/worktime.yaml`, generated `WorkTimeEmployeeApi`).
- Employment periods live on the persisted `User` aggregate. Regular working times are user-BC domain values resolved on demand from ZEP, as required by the authoritative user-sync/provider-detail specs.
- The `ProjectEntry` booking family is also consumed by `MonthlyReportServiceImpl`/`WorkingTimeUtil`, so it stays in legacy; the hexagon gets its own copies.
- The legacy warning models (`TimeWarning`, `JourneyWarning`, `ProjectEntryWarning`, `TimeWarningType`, `JourneyWarningType`, `WarningType`, `MappedTimeWarningTypes`, `WorkTimeBookingWarning` + DTO + mapper) are used **only** by the warnings path, so they can be deleted with it.

## Goals / Non-Goals

**Goals:**
- Relocate the twelve calculators into `worktime/domain/services/warning` via `git mv`, preserving decision logic and (best-effort) git history.
- Replace the input/output models with hexagon-owned records/sealed types.
- Stop computing localized text in the backend; expose warnings as typed, dated, quantified facts.
- Reframe the no-time-entry rule as a pure set difference, with master data and the clock supplied by the application layer.
- Add a self-scoped employee warnings endpoint to the canonical work time contract.
- Remove the legacy warnings path.

**Non-Goals:**
- No project-lead warnings view (self-employee only, mirroring legacy scope).
- No change to the legacy `ProjectEntry` family or `getProjectTimes` (still used by monthly report).
- No change to the twelve calculators' algorithms — only inputs, outputs, and construction.
- Frontend implementation is out of this repo (coordination only).

## Decisions

### D1 — Input model: sealed `WorkTimeBooking`, hexagon-owned vocabulary
Replace the `ProjectEntry` interface + subclasses with:

```
worktime/domain/model/
  sealed interface WorkTimeBooking permits ProjectBooking, JourneyBooking
      LocalDateTime from(); LocalDateTime to(); Task task();
      WorkingLocation workingLocation(); boolean workLocationProjectRelevant();
      default LocalDate date();  default double durationInHours();
  record ProjectBooking(… , String process)                    implements WorkTimeBooking
  record JourneyBooking(… , JourneyDirection direction, Vehicle vehicle) implements WorkTimeBooking
  enum Task | Vehicle | WorkingLocation | JourneyDirection   (hexagon copies)
```

Calculators change `entry instanceof JourneyTimeEntry jte` → `case JourneyBooking jb` (pattern switch on the sealed type). The vocabulary enums (`Task`, `Vehicle`, `WorkingLocation`, `JourneyDirection`) are re-declared in the worktime BC — the BC owns its language and must not depend on legacy `domain.model.monthlyreport`. Raw ZEP-code → enum mapping lives in the outbound adapter (ported from the legacy `zep/rest/mapper/ProjectEntryMapper`).

**Alternative rejected:** `git mv` the legacy `ProjectEntry` family into the hexagon. Rejected because that family is still used by monthly report; moving it would either break legacy or force the hexagon to depend on legacy.

### D2 — Output model: flat `WorkTimeWarning` record, no messages
```
worktime/domain/model/
  record WorkTimeWarning(LocalDate date, WorkTimeWarningType type, Double hours)
  enum WorkTimeWarningType { OUTSIDE_CORE_WORKING_TIME, TIME_OVERLAP, NO_TIME_ENTRY,
      EMPTY_ENTRY_LIST, HOLIDAY, WEEKEND, WRONG_DOCTOR_APPOINTMENT,
      EXCESS_WORKING_TIME_PRESENT, MISSING_REST_TIME, MISSING_BREAK_TIME,
      BACK_MISSING, TO_MISSING, INVALID_WORKING_LOCATION, LOCATION_RELEVANT_SET }
```

`hours` is non-null only for `MISSING_BREAK_TIME` / `MISSING_REST_TIME` / `EXCESS_WORKING_TIME_PRESENT`; `date` is null only for `EMPTY_ENTRY_LIST`. The enum has **no** `messageTemplate()`; the legacy `TimeWarningType`/`JourneyWarningType` are unified into one enum, **constant names preserved** so transloco keys keep resolving. Each calculator's construction tail becomes `new WorkTimeWarning(date, TYPE, null)`.

### D3 — Naming
Aggregate-level worktime types carry the `WorkTime` prefix (matching `WorkTimeReport`, `WorkTimeAttendance`): `WorkTimeWarning`, `WorkTimeWarningType`, `WorkTimeBooking`. The sealed leaves stay unprefixed (`ProjectBooking`, `JourneyBooking`) — unambiguous within the hierarchy. Casing is `WorkTime` (capital T), per existing convention.

### D4 — Calculator interface collapse + assembler
Collapse `WarningCalculationStrategy<T>` and the two parallel manager methods into one contract:

```
worktime/domain/services/warning/
  interface WorkTimeWarningCalculator { List<WorkTimeWarning> calculate(List<WorkTimeBooking> bookings); }
  (11 relocated calculators implement it)
  NoEntryWarningCalculator                    (distinct signature — see D5)
  WorkTimeWarningAssembler                    (concatenates calculators → flat list; no merge, no i18n)
```

The `AbstractTimeWarningCalculationStrategy` grouping/duration helpers move alongside the calculators (as a shared base or small helper). `WarningCalculatorsManager` is deleted.

### D5 — No-time-entry reframed as a set difference; master data behind a port
`NoEntryWarningCalculator` becomes pure:

```
noEntry(expectedWorkingDays, bookedDates, excusedDates, today) =
    expectedWorkingDays − bookedDates − excusedDates − {d : d ≥ today}
```

- **`WorkTimeExpectedWorkingDaysPort`** (new outbound) → `Set<LocalDate> expectedWorkingDays(UserId, YearMonth)`. Its adapter resolves office-calendar working days ∩ active employment-period start ∩ non-zero-hour weekdays. **Cross-BC sourcing (resolved):** the adapter injects the user BC's `UserRepository` outbound port (exactly as the existing `WorkTimeUserSnapshotAdapter` does) for the active employment period and uses the established on-demand ZEP regular-working-time lookup required by the main user-provider-detail contract. Provider DTOs are translated into the user BC's `RegularWorkingTimes`/`RegularWorkingTime` values inside the outbound adapter, and `RegularWorkingTimes.active(YearMonth)` selects the effective schedule. The adapter intersects those facts with `OfficeCalendarUtil` and returns a plain `Set<LocalDate>`; no user-BC or provider type reaches the worktime domain.
- **`excusedDates`** = absences from `WorkTimeAbsenceZepPort` filtered to exclude `HOME_OFFICE` (parity: legacy did not excuse HO).
- **`today`** = `LocalDate.now(clock)` derived in the application service from an injected `java.time.Clock` and passed in (honors the project clock-injection rule).
- Empty bookings short-circuit to a single `EMPTY_ENTRY_LIST` warning (in the service/assembler), matching legacy.

**Alternative rejected:** extend the user snapshot to carry employment periods + regular working times and keep day-derivation inside the calculator. Rejected — bloats the shared `UserRef` and drags master-data interpretation into the domain calculator.

### D6 — Application service + inbound REST
```
worktime/application/port/inbound/GetEmployeeWarningsUseCase   getWarnings(UserId, YearMonth) → List<WorkTimeWarning>
worktime/application/GetEmployeeWarningsService                (@ApplicationScoped @Transactional, injects Clock)
      fetch: WorkTimeBookingZepPort (new) + WorkTimeAbsenceZepPort (reuse) + WorkTimeExpectedWorkingDaysPort (new)
      run:   WorkTimeWarningAssembler
worktime/adapter/inbound/rest/WorkTimeEmployeeResource  (EXISTING — add getEmployeeWarnings; @MegaRolesAllowed(EMPLOYEE), actorId from AuthenticatedActorContext)
      WorkTimeWarningRestMapper: List<WorkTimeWarning> → flat DTO [{ type, date, hours }]
```

The BC groups inbound resources by **actor**, not by sub-feature: `WorkTimeEmployeeResource` and `WorkTimeProjectLeadResource` each aggregate all endpoints for their actor (tags `WorkTimeEmployee` / `WorkTimeProjectLead`). The warnings endpoint is employee-scoped, so it is a new method on the existing `WorkTimeEmployeeResource` implementing the same generated `WorkTimeEmployeeApi` — not a new resource class. Concerns stay separated at the use-case and mapper level (`GetEmployeeWarningsUseCase`, `WorkTimeWarningRestMapper`), keeping the thin resource an actor-level aggregator. This matches the shared, actor-spanning `worktime-rest-api` capability the delta spec targets.

`WorkTimeBookingZepPort.fetchBookingsForEmployee(zepUsername, month)` reuses the existing `AttendanceService` fetch but maps to `WorkTimeBooking` (the rich mapping) instead of the aggregate `WorkTimeAttendance`.

### D7 — Wire contract: flat `{ type, date?, hours? }`, frontend groups by type
The OpenAPI response is a flat array, mapping 1:1 to `WorkTimeWarning`. `type` = the preserved enum name. The frontend groups by type for its chip rendering (a trivial computed/pipe) and keeps its transloco keys. Added to `openapi/paths/worktime.yaml` (`/worktime/warnings/{payrollMonth}`) + a `WorkTimeWarning` schema in `openapi/schemas/worktime.yaml`.

**Alternative considered:** preserve the legacy grouped shape `[{ name, warningDates:[{date,hours}] }]` so the frontend needs zero changes. Rejected in favor of the cleaner flat model (the frontend must repoint to the new URL anyway); grouping moves to the client.

## Risks / Trade-offs

- **Behavioural regression during model swap** → Port the existing calculator unit tests alongside the `git mv`, re-expressed against `WorkTimeBooking`/`WorkTimeWarning`; assert per-calculator parity with legacy scenarios before deleting the legacy path.
- **`git mv` history fidelity when imports/types change** → Accepted as nice-to-have (blame is preserved even with edits). Do the pure `git mv` as its own commit, then edit in a follow-up commit so `--follow` tracks cleanly.
- **Coordinated frontend cutover** → Removing the legacy endpoint breaks the frontend until it repoints. Resolved: the frontend adapts to the new endpoint immediately, so legacy removal ships in this change (no staging flag) and backend + frontend land together.
- **`ExpectedWorkingDays` master-data source** → resolved (see D5): the adapter reads the `User` aggregate via the user BC's `UserRepository` outbound port. Verify an employee with no active employment period for the month is handled (legacy threw `IllegalStateException`); decide whether to preserve that or return an empty expected-days set.
- **Parity subtleties to pin in tests:** `HOME_OFFICE` excluded from excused days; `EMPTY_ENTRY_LIST` short-circuit; the three quantitative types keep their hours; `WRONG_DOCTOR_APPOINTMENT` naming; future-day exclusion uses injected clock.

## Migration Plan

1. Add hexagon vocabulary enums + sealed `WorkTimeBooking` (`ProjectBooking`/`JourneyBooking`) and `WorkTimeWarning`/`WorkTimeWarningType`.
2. `git mv` the twelve calculators + `AbstractTimeWarningCalculationStrategy` into `worktime/domain/services/warning` (history-preserving commit), then adapt imports, sealed-switches, and construction tails (parity-preserving commit). Move/port their unit tests.
3. Add `WorkTimeBookingZepPort` + adapter (rich `ZepAttendance` → `WorkTimeBooking` mapping) and `WorkTimeExpectedWorkingDaysPort` + adapter.
4. Add `WorkTimeWarningAssembler`, `GetEmployeeWarningsUseCase` + `GetEmployeeWarningsService` (inject `Clock`).
5. Extend the OpenAPI contract (path + schema under the `WorkTimeEmployee` tag), add the `getEmployeeWarnings` method to the existing `WorkTimeEmployeeResource` + a `WorkTimeWarningRestMapper`, regenerate.
6. Remove the legacy warnings path: `WarningCalculatorsManager`, `TimeWarningService`(+impl), legacy warning models + warning-type enums, `WorkTimeBookingWarning`(+Dto+Mapper), and the `getAllWarningsForEmployeeAndMonth` endpoint method.
7. Coordinate the frontend repoint (out-of-repo).

**Rollback:** steps 1–5 are purely additive; the new endpoint can be reverted without touching legacy. Step 6 (legacy removal) ships in this change alongside the frontend cutover — reverting after step 6 means restoring the legacy path and repointing the frontend back.
