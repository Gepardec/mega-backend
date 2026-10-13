# Design

## Context

See proposal.md, "Why", for the failure and why it happens.

Constraints that shape the approach:

- **ArchUnit** (`HexagonalArchitectureTest`):
  - Every `*UseCase` implementation in `..hexagon..application..` must carry `@Transactional`. The rule does not check the `TxType`.
  - `@Transactional` is forbidden on any class or method outside `..application..`, so adapters cannot open their own transactions.
  - The `Service` naming and `@ApplicationScoped` rules apply only to `*UseCase` implementations.
- **Quarkus Hibernate ORM** allows reads without a JTA transaction as long as a request context is active. REST requests and `@Scheduled` methods both have one.
- **`quarkus-smallrye-context-propagation`** is on the classpath and propagates the JTA context into Mutiny callbacks by default.
- **Unit tests** for application services are plain JUnit + Mockito with no CDI container. Anything that needs Arc at runtime, such as `QuarkusTransaction`, cannot be called directly from service code without breaking these tests.

Affected services and their current state:

| Service | Local DB access | Fan-out | State |
|---|---|---|---|
| `worktime/GetProjectLeadWorkTimeService` | reads | ZEP memberships + attendances (`Multi` merge, `await`) | **done**: `TxType.NEVER` |
| `worktime/GetEmployeeWorkTimeService` | reads | ZEP attendances (single `Uni`, `await`) | **done**: `TxType.NEVER` |
| `user/UpdateInternalRatesService` | reads | ZEP hourly-rate writes (`Multi` merge, `await`) | **done**: `TxType.NEVER` |
| `user/UpdateReleaseDatesService` | reads + **writes** | ZEP release-date writes | open |
| `user/AutoUpdateReleaseDatesService` | reads + **writes** | ZEP release-date writes | open |

## Goals / Non-Goals

**Goals:**
- No JTA transaction is open while ZEP calls run concurrently in any hexagon use case.
- Each employee's release-date update is saved in its own transaction, so one failed save cannot roll back another employee's.
- No database access on Vert.x I/O threads.

**Non-Goals:**
- Changing the global context-propagation configuration.
- Making the ZEP write and the local save atomic. ZEP is not a transactional resource. The current behaviour of "ZEP first, then save locally" stays as it is.
- Reworking the legacy (`com.gepardec.mega` outside `hexagon`) services.
- Adding an ArchUnit rule that detects `@Transactional` combined with Mutiny fan-out. That can't be detected reliably with static rules. See Risks.

## Decisions

### D1: `@Transactional(TxType.NEVER)` on fan-out use cases

Without a transaction, the propagated JTA context is empty and nothing gets committed when the method returns, so the race cannot happen. `NEVER` still satisfies the ArchUnit annotation rule, and it documents the intent: these use cases must not run inside a transaction. If a transactional caller is ever added, the method fails immediately with a `TransactionalException` instead of reintroducing the race unnoticed.

Each annotated class gets a one-line comment explaining why, so the annotation isn't "fixed" back to the default.

**Alternatives considered:**
- *`TxType.NOT_SUPPORTED`*: also works, but silently suspends a caller's transaction and hides misuse. Rejected in favour of failing fast.
- *Clear the transaction context globally (`mp.context.ThreadContext.cleared=Transaction`)*: a global change with effects on unrelated code, and harder to reason about. Rejected.
- *Make the ZEP ports blocking*: gives up the concurrency the specs require for release-date updates and that keeps the worktime reports fast. Rejected.

### D2: Load users before the fan-out (`UpdateReleaseDatesService`)

Today `updateInZep` calls `userRepository.findById` inside `transformToUniAndMerge`. When the number of commands exceeds the merge concurrency, Mutiny pulls further items, and calls the mapper, from the thread that completed an earlier inner `Uni`, which is a Vert.x I/O thread. The service will instead resolve all users up front with the existing `UserRepository.findByIds(Set<UserId>)` on the calling thread, and pass the resolved `User` into the fan-out. Unknown users and users without a ZEP username are marked as failed before the fan-out starts, as they are today.

`AutoUpdateReleaseDatesService` already loads its users before the fan-out and needs no change here.

### D3: Save each user in its own transaction via a dedicated application-layer bean

A new `@ApplicationScoped` bean in `hexagon/user/application` (working name `ReleaseDatePersister`) exposes one method that saves a single `User`, annotated `@Transactional(TxType.REQUIRES_NEW)`. Both release-date services call it once per successful ZEP outcome, after the fan-out has finished, from the calling thread.

The `try/catch` around each save stays in the service, outside the bean's transactional proxy. This matters because the flush happens at commit, inside the interceptor. Commit failures come out of the proxy as an unchecked exception (`ArcUndeclaredThrowableException` wrapping `RollbackException`), so catching `RuntimeException` at the call site covers constraint violations during flush as well as commit failures. Only that one user is rolled back and reported as failed.

`REQUIRES_NEW` rather than `REQUIRED`: the callers run without a transaction (D1), so the two behave the same today. `REQUIRES_NEW` makes "one transaction per user" explicit and stays correct even if the call site changes.

**Alternatives considered:**
- *`QuarkusTransaction.requiringNew().run(...)` inside the service*: shorter, but the static call needs Arc at runtime and breaks the Mockito unit tests. Rejected.
- *Self-invocation of a `@Transactional` method on the same service*: CDI interceptors don't apply to self-calls, so no transaction would be opened. Rejected.
- *`@Transactional` on the persistence adapter*: forbidden by ArchUnit (transaction boundaries belong in the application layer). Rejected.
- *Keep one transaction for the whole batch, opened after the fan-out*: removes the race, but one bad row still rolls back the rest, which violates the `user-rest-api` failure-isolation requirement. Rejected.

## Risks / Trade-offs

- **[Reads outside a transaction see no consistent snapshot across queries]** → acceptable. The affected reads are independent lookups for reporting or to resolve targets. Nothing relies on repeatable-read semantics.
- **[A future use case adds `@Transactional` + Mutiny fan-out and brings the race back]** → the class comments, and this design doc as a reference. A static ArchUnit check (for example "classes depending on `io.smallrye.mutiny` must not use the default `TxType`") is possible but crude. It can be added later if this pattern spreads.
- **[ZEP write succeeds, local save fails]** → unchanged from today. The user is reported in `failedUserIds`, and the next user sync (`user-sync`) brings the local value back in line with ZEP.
- **[Per-user transactions cost more round-trips for large batches]** → batches are small (manual office-management input, or monthly active users in the scheduler). Negligible.
- **[`TxType.NEVER` throws if someone later calls a use case from transactional code]** → intended: fail fast (D1). The exception message names the cause.

## Migration Plan

No data or API migration. Deploy normally. Rolling back means reverting the commit. Nothing persistent depends on the change.
