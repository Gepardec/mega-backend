## Context

The legacy `SyncServiceImpl.syncUpdateEmployeesWithoutTimeBookingsAndAbsentWholeMonth()` detects employees absent for an entire payroll month by querying ZEP absences directly, then sets `StepEntry` records to DONE with a flat German reason string. This logic lives entirely in the legacy `com.gepardec.mega` package and has no equivalent in the hexagonal BCs.

The hexagonal `monthend` BC already models `MonthEndTask` (with `subjectEmployeeId`, `eligibleActorIds`, `complete(UserId)`) and `MonthEndClarification` (created by the subject employee or a project lead). Both domain models assume a human actor, making automated system-initiated operations impossible without design changes.

The `worktime` BC owns ZEP time data but does not yet expose absence querying as a formal use case. The `monthend` BC has no outbound port for absences.

## Goals / Non-Goals

**Goals:**
- Migrate the absent-employee auto-completion workflow into the hexagonal BCs
- Introduce a first-class system actor identity (`Role.SYSTEM`) usable across all hexagonal BCs
- Establish the Customer-Supplier + ACL cross-BC integration pattern between `worktime` (supplier) and `monthend` (customer) for absence data
- Create one `MonthEndClarification` per assigned project (replacing the legacy flat reason string)
- Preserve full audit trail honesty: `completedBy` and `createdBy` reflect `SystemActor.USER_ID`, not the employee

**Non-Goals:**
- Migrating or removing the legacy `SyncServiceImpl` method (parallel operation until full legacy sunset)
- Exposing absence data via REST API
- Handling partial-month absences or complex absence patterns beyond "absent every working day"

## Decisions

### D1: System actor as a seeded `hexagon_users` row with `Role.SYSTEM`

**Decision:** The MEGA system actor is a real row in `hexagon_users` with a well-known UUID, `Role.SYSTEM`, and null ZEP/Personio/email fields. Exposed as `SystemActor.USER_ID` in `shared/domain/model`.

**Rationale:** Keeps a uniform identity model — everything that acts on data has a `UserId`. Audit trail queries are pure SQL. The system user is naturally excluded from snapshot queries because `EmploymentPeriods.empty()` causes `isActiveIn()` to return false. `UserRef` remains a human-only snapshot type; REST adapters resolve `SystemActor.USER_ID` to "MEGA System" as a special display case.

**Alternatives considered:**
- *Sealed `Principal` interface (`HumanPrincipal | SystemPrincipal`)*: Type-safe but requires refactoring both domain records, mappers, entities, and all REST DTOs. High cost for a problem that can be solved with one `||` clause.
- *System impersonates the subject employee*: Simpler but semantically wrong — the employee would inherit `canDelete`/`canEditText` rights over a clarification they did not author. Rejected.

### D2: `completeBySystem()` and `createBySystem()` as separate domain methods

**Decision:** `MonthEndTask` gets `completeBySystem()` alongside the existing `complete(UserId actorId)`. `MonthEndClarification` gets `createBySystem()` alongside the existing `create()`. The canonical constructor validation for `completedBy`/`createdBy` is relaxed to permit `SystemActor.USER_ID`.

**Rationale:** Named methods make the intent explicit at the call site. The bypass is documented in the domain, not hidden in an adapter. `canDelete` is tightened: `createdBy.equals(SystemActor.USER_ID)` → returns false, preventing employees from deleting system-generated clarifications.

**Alternatives considered:**
- *Include `SystemActor.USER_ID` in `eligibleActorIds` at task creation*: Too permissive — the system could complete any task at any time, not just for absent employees.
- *`completeBySystem()`skips canonical constructor by using a separate private constructor*: Avoids touching validation logic but breaks the "canonical constructor validates all invariants" guarantee. Rejected.

### D3: `worktime` BC as upstream supplier for absence data (Customer-Supplier + ACL)

**Decision:** The `worktime` BC adds `GetEmployeeAbsencesUseCase` as a formal inbound use case. The `monthend` BC defines `MonthEndEmployeeAbsencePort` as its own outbound port. A new `MonthEndWorkTimeAbsenceAdapter` (ACL) implements the port by calling the `worktime` use case and filtering absence types.

**Rationale:** `Absence` is semantically a worktime concept. Centralising it in `worktime` avoids duplicating ZEP absence logic. The ACL pattern protects `monthend`'s domain from `worktime`'s model — the `monthend` application service only sees its own port. The rule "never call another BC's inbound use case port from an application service" is respected: the cross-BC call happens in an adapter (infrastructure layer), not in the application service.

**Alternatives considered:**
- *Each BC talks to ZEP independently*: Duplicates ZEP call logic and splits the owning model. Rejected.
- *`Absence` in `shared/domain/model`*: Shared kernel creates coupling and tends to grow. The DDD guidance warns against it. Rejected.

### D4: Absence type filtering split between adapter (ZEP) and application service (business rule)

**Decision:** The `WorkTimeAbsenceZepAdapter` returns all ZEP absence types faithfully. The `MonthEndWorkTimeAbsenceAdapter` (ACL) filters out `HOME_OFFICE` and `EXTERNAL_TRAINING` — absence types where working time is still required. The `CompleteTasksForAbsentEmployeeService` owns the "every working day covered?" check via `OfficeCalendarUtil.isWorkingDay()`.

**Rationale:** Which absence types count for payroll auto-confirmation is a `monthend` policy, not a `worktime` or ZEP concern. Keeping it in the ACL adapter or the application service preserves `worktime`'s neutrality.

### D5: Scheduler runs at 17:00 on the last working day, operates on the current month

**Decision:** `AbsentEmployeeMonthEndScheduler` runs at **17:00 on the last day of the month** (cron `L`), checked against `OfficeCalendarUtil.isLastWorkingDayOfMonth`. `MonthEndTaskGenerationScheduler` runs earlier in the day (its existing schedule is unchanged). It operates on `YearMonth.now()` (the current month, same as task generation).

**Rationale:** Scheduling the absent-employee check at end-of-business (17:00) guarantees task generation has already run earlier that day, eliminating the race condition without requiring explicit scheduler ordering. Operating on the current month (not `now().minusMonths(1)`) aligns with how the hexagonal task model is structured.

### D6: Per-employee atomic use case, scheduler does the fan-out

**Decision:** `CompleteTasksForAbsentEmployeeUseCase` takes a single `(UserId, YearMonth)` pair and returns `Optional<AbsentEmployeeAutoCompletion>`. The scheduler calls it once per active user.

**Rationale:** Same pattern as `PrematureMonthEndPreparationService`. Keeps the use case testable in isolation. The scheduler is responsible for fan-out and logging, not the use case.

## Risks / Trade-offs

**[Race condition: scheduler runs before tasks are generated]** → ~~Both schedulers run on the last working day with no guaranteed execution order.~~  
*Resolved (D5):* `AbsentEmployeeMonthEndScheduler` runs at 17:00 — well after `MonthEndTaskGenerationScheduler`, which runs earlier in the day. No explicit ordering mechanism required.

**[System user appears in `findByIds()` queries]** → `MonthEndUserSnapshotPort.findByIds()` is used in REST adapters to resolve actor display names. If `SystemActor.USER_ID` is passed, the `UserSnapshotAdapter` calls `UserRepository.findByIds()` which returns the system user. MapStruct will attempt to map it via `MonthEndUserSnapshotMapper` which calls `user.zepUsername()` — null for the system user.  
*Mitigation:* REST adapters that resolve `completedBy`/`createdBy` must handle `SystemActor.USER_ID` as a special display case before delegating to the snapshot query.

**[`User` invariant weakening]** → Making `zepUsername` and `email` conditionally nullable in the `User` record weakens a previously unconditional guarantee. Future code that calls `user.zepUsername()` without a null check could NPE if it receives the system user.  
*Mitigation:* The system user is never returned by `isActiveIn()` queries; document the invariant clearly. Consider a helper method `user.isSystemActor()` to make the check explicit.

## Migration Plan

1. Add `Role.SYSTEM` and `SystemActor.USER_ID` — no behaviour change, purely additive
2. Add Liquibase changelog seeding the system actor row
3. Update `User` domain invariant (conditional null-check)
4. Add `completeBySystem()` / `createBySystem()` to domain models — additive, no existing callers affected
5. Add `worktime` BC absence use case and ZEP adapter
6. Add `monthend` absence port and ACL adapter
7. Add `CompleteTasksForAbsentEmployeeService` and use case port
8. Add `AbsentEmployeeMonthEndScheduler`
9. The legacy `SyncServiceImpl` method continues to run in parallel until the legacy sunset

No rollback complexity — all new code paths. The legacy method is unmodified and remains the safety net.

## Open Questions

~~**Scheduler ordering**: Should explicit ordering between `MonthEndTaskGenerationScheduler` and `AbsentEmployeeMonthEndScheduler` be enforced, or is a warning log on "absent but no tasks found" sufficient?~~  
→ *Closed:* Resolved by scheduling `AbsentEmployeeMonthEndScheduler` at 17:00 (see D5).

~~**Clarification text**: Should the auto-generated clarification text ("Aufgrund von Abwesenheiten wurde der Monat automatisch bestätigt.") be externalised to configuration, or is hardcoding acceptable for now?~~  
→ *Closed:* Defined as a named constant (e.g., `SYSTEM_CLARIFICATION_TEXT`) in `CompleteTasksForAbsentEmployeeService`. Not externalised to configuration.
