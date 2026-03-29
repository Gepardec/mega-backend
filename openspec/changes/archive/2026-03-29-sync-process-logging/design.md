## Context

`SyncScheduler` sequences `SyncUsersUseCase → SyncProjectsUseCase → ReconcileLeadsUseCase` every 30 minutes. All three use cases currently return `void`, leaving `SyncScheduler` with nothing to log beyond "started" / "finished". Operations like a full re-run after ZEP data issues are indistinguishable from a clean no-op run.

Quarkus standard logging (`org.jboss.logging.Logger`) is already in use. Timing infrastructure in the JDK (`Instant`, `Duration`) covers the elapsed-time requirement without new dependencies.

## Goals / Non-Goals

**Goals:**
- Each use case returns a typed result record with operation counts
- `SyncScheduler` logs one structured summary line per step (counts + elapsed ms) and one totals line per full cycle
- Logging uses `INFO` level so it appears in production logs without configuration changes

**Non-Goals:**
- Metrics/Micrometer instrumentation (out of scope — logs are sufficient for now)
- Persisting sync history to the database
- Exposing sync results via REST endpoint
- Changing error-handling or retry behaviour

## Decisions

### Result records are plain Java records in the domain layer

Each use case interface defines its own result record (`UserSyncResult`, `ProjectSyncResult`, `ReconcileLeadsResult`) co-located with the use case interface. Records are pure data; no Quarkus dependencies. This keeps the domain clean and makes results easy to test.

**Alternative considered**: A shared `SyncResult` base type in a `common` package. Rejected — the three operations track different counters (e.g. "disabled" only applies to users) and a shared type would either be too generic or force nullable fields.

### Timing is measured in `SyncScheduler`, not in the use cases

`SyncScheduler` captures `Instant.now()` before and after each use case call and computes `Duration.between()`. The use cases remain unaware of timing; they focus on domain logic and return counts.

**Alternative considered**: Including elapsed time inside each result record. Rejected — timing is an infrastructure concern, not a domain concern. The scheduler is the right place to own it.

### Log format: one INFO line per step, one INFO summary line per cycle

```
[SyncScheduler] user-sync: added=3 updated=12 disabled=1 (duration=342ms)
[SyncScheduler] project-sync: created=0 updated=47 (duration=891ms)
[SyncScheduler] reconcile-leads: resolved=89 skipped=2 rolesAdded=1 rolesRevoked=0 (duration=210ms)
[SyncScheduler] sync cycle complete: total duration=1443ms
```

Key-value pairs are machine-friendly and easy to `grep`. Using `INFO` (not `DEBUG`) ensures visibility in production without toggling log levels.

## Risks / Trade-offs

- **Return type change is a breaking API change within the hexagonal package** → All callers of the three use case interfaces must be updated (currently only `SyncScheduler`); straightforward.
- **Count accuracy depends on service implementation** → Services must correctly increment counters per operation. Unit tests on each service will verify this.

## Migration Plan

1. Add result record types alongside their use case interfaces
2. Update service implementations to accumulate and return counts
3. Update `SyncScheduler` to capture timing, consume results, and emit log lines
4. Update any existing unit/integration tests that assert on `void` return types
