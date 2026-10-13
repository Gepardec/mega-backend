# Proposal

## Why

Hexagon application services that call ZEP concurrently while a JTA transaction is open fail intermittently at commit time (`ARJUNA012107 … commiting with 2 threads active`, `Enlisted connection used without active transaction`). The project-lead worktime report already fails this way in production. Smallrye context propagation copies the open transaction onto the Vert.x threads that run the ZEP response callbacks. When the service waits for the results and returns, one of those threads can still be attached to the transaction during the commit, so the commit is rejected and the request ends in a 500.

The two release-date services have the same race. They also save every user in one shared transaction, so a single failed save rolls back release dates the service already reported as successful. That breaks the existing requirement that one employee's failure must not affect the others.

## What Changes

- Use `@Transactional(TxType.NEVER)` on the use-case services that fan out to ZEP, so no transaction is open while the concurrent calls are in flight. The ArchUnit rule that every use-case implementation carries `@Transactional` still passes.
  - Already applied: `GetProjectLeadWorkTimeService`, `GetEmployeeWorkTimeService`, `UpdateInternalRatesService` (they only read locally).
  - Still to do: `UpdateReleaseDatesService`, `AutoUpdateReleaseDatesService`.
- Release-date services: load all affected users before the ZEP fan-out, and save each successful update in its own short transaction through a dedicated transactional bean in the user application layer.
- No change to external behaviour, endpoints or response shapes. Behaviour that was already specified but violated by the implementation is now actually met.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. The existing requirements already describe the intended behaviour, and this change makes the implementation match them:
- `user-rest-api`, "Release date updates are executed concurrently and failures are isolated"
- `user-aggregate`, "Release date is written to ZEP and immediately reflected locally"

The change therefore sets `skip_specs: true`.

## Impact

- **Code**: `hexagon/worktime/application` (2 services, already changed), `hexagon/user/application` (3 services, 1 already changed, plus a new transactional persistence bean).
- **Transaction semantics**: release-date saves move from one transaction for the whole batch to one transaction per user.
- **Callers**: `UserResource`, `WorkTimeEmployeeResource`, `WorkTimeProjectLeadResource`, `ReleaseDateAutoUpdateScheduler`. None of them opens a transaction, so `TxType.NEVER` is safe for them.
- **Tests**: unit tests for the two release-date services need updating for the new collaborator. Architecture tests are unaffected.
- No API, database schema or dependency changes.
