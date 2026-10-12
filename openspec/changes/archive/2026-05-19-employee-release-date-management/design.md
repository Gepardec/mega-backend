## Context

Release-date management currently lives entirely in the legacy `com.gepardec.mega` stack: `EmployeeServiceImpl.updateEmployeesReleaseDate` fans out via `CompletableFuture.allOf` and delegates to `ZepSoapServiceImpl`, which is the only implementation (`ZepRestServiceImpl` throws `NotImplementedException`). There is no scheduled automation — office management triggers updates manually via a legacy REST endpoint.

The hexagonal `user` BC already has `ZepEmployeePort` (read-only: `fetchAll()`) and `UserRepository`. `ZepEmployeeRestClient` is fully Mutiny-based (`Uni<>`). The `monthend` BC owns `MonthEndTaskRepository` with `findByMonth(YearMonth)` and per-employee task queries.

## Goals / Non-Goals

**Goals:**
- Spec-first REST API for office management to list active employees and bulk-update their ZEP release dates
- Replace CompletableFuture fan-out with Mutiny `Multi` concurrent fan-out; Mutiny terminates at the application service boundary (`.await().indefinitely()`), matching the existing pattern in `GetEmployeeWorkTimeService`
- Automated daily scheduler (from 15th of month) that updates each employee's release date in ZEP as soon as all their month-end tasks for the previous payroll month are complete
- ZEP write goes via REST first; SOAP remains available as a verified fallback

**Non-Goals:**
- Removing or modifying legacy `updateEmployeesReleaseDate` / SOAP code
- Making use-case interfaces reactive (ports remain synchronous at the inbound boundary; `Uni` exists only on outbound ZEP ports)

## Decisions

### 1. User BC owns the auto-update scheduler

The scheduler lives in `user/adapter/inbound` and invokes `AutoUpdateReleaseDatesUseCase`. The use case declares a `PayrollMonthCompletionPort` outbound port in `user/domain/port/outbound`; the monthend BC adapter implements it using `MonthEndTaskRepository`.

**Alternative considered:** Monthend BC owns the scheduler → it would need its own ZEP write port, creating parallel ZEP infrastructure and entangling monthend with user mutations. Rejected.

The cross-BC dependency flows one way: user BC declares the port contract, monthend BC fulfills it. This follows the existing cross-BC pattern in the project.

### 2. Per-employee independent completion check

Each active employee is checked independently. If their tasks are all DONE, their release date is updated immediately. Employees with open tasks are skipped and retried on the next daily run.

**Alternative considered:** All-or-nothing (wait until every employee is done before updating anyone) → slower rollout, penalises prompt employees because of laggards. Rejected.

### 3. Mutiny depth: port returns `Uni<Void>`, service terminates with `.await()`

`ZepEmployeePort.updateReleaseDate(ZepUsername, LocalDate)` returns `Uni<Void>`. The adapter maps directly to `ZepEmployeeRestClient.updateEmployee(...)`. The application service uses:

```java
Multi.createFrom().iterable(commands)
    .onItem().transformToUniAndMerge(cmd -> resolveAndUpdate(cmd))
    .collect().asList()
    .await().indefinitely();
```

This gives true concurrent fan-out without blocking a thread per call, matching the semantics of the old `CompletableFuture.allOf`. Mutiny does not propagate above the service boundary — use-case interfaces return plain types.

### 4. Release date value for the scheduler

The scheduler always sets the release date to the last calendar day of the previous (payroll) month: `YearMonth.now().minusMonths(1).atEndOfMonth()`. No configurable offset. The manual REST endpoint accepts an explicit date from the caller.

### 5. ZEP write: REST first, SOAP fallback

A new `PUT /{username}` is added to `ZepEmployeeRestClient` with a dedicated minimal request body `ZepEmployeeUpdateRequest { @JsonProperty("release_date") LocalDate releaseDate }`. If the ZEP REST endpoint proves unavailable in testing, the adapter falls back to the existing SOAP path via `ZepService.updateEmployeesReleaseDate`.

### 6. Release date is dual-written: ZEP first, then local DB

After a successful ZEP `PUT /{username}` call, the service immediately persists the new `release_date` on the local `User` entity via `UserRepository`. This ensures `GET /users/active` returns the current release date without waiting for the next sync run.

Write order: ZEP → local DB. If the ZEP call fails, no local write occurs and the user ID is added to `failedUserIds`. If the ZEP call succeeds but the local write fails, the failure is logged at `ERROR` — ZEP is the source of truth, so this is recoverable by the next sync.

`release_date` is added to:
- `ZepEmployeeSyncData` (nullable `LocalDate`) — so the sync flow also populates it
- `User` record (nullable `LocalDate releaseDate`)
- `UserEntity` (nullable `LocalDate releaseDate`, column `release_date`)
- A new Liquibase changelog entry adds `release_date DATE NULL` to `hexagon_users`

**Alternative considered:** Sync-only (no dual-write) — release date becomes stale between sync runs, making the frontend display misleading immediately after an update. Rejected.

### 7. Active employees scoped to previous payroll month

`GET /users/active` returns users whose employment periods overlap the previous payroll month (`UserRepository.findAll()` filtered by `user.isActiveIn(payrollMonth)`). The previous month is the natural scope for the release-date workflow. The response includes each user's locally stored `releaseDate` (nullable).

## Risks / Trade-offs

- **ZEP REST PUT unconfirmed** → Adapter is written against the REST client; SOAP call is available as a one-line fallback if the endpoint does not behave as expected. Risk is low-effort to mitigate.
- **Cross-BC port dependency** → `PayrollMonthCompletionPort` is declared in `user/domain/port/outbound` and implemented in `monthend/adapter/outbound`. The interface must not import any monthend domain types — only shared types (`UserId`, `YearMonth`). This is a deliberate, narrow contract.
- **`@Transactional` + dual-write ordering** → ZEP calls are external HTTP and do not participate in the JPA transaction. Write order is ZEP first, then local DB. A ZEP success followed by a local DB failure leaves ZEP ahead of local state; the next sync run will reconcile. This window is acceptable — ZEP is the source of truth.
- **Idempotency of the scheduler** → Running multiple times on the same day for the same employee is safe: the ZEP PUT is idempotent (same date, same username). The scheduler does not track "already updated" state.
