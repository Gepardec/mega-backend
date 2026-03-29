## Context

The hexagonal domain introduced in `user-foundation` has `EmploymentPeriod` and `RegularWorkingTime` as plain value-object records. The `ZepProfile` aggregate holds them as raw `List<T>`, meaning any caller that wants to ask "is this employee active on date X?" must implement the traversal logic themselves or reach back to the legacy domain package.

The legacy package already has well-tested aggregate classes — `EmploymentPeriods` and `RegularWorkingTimes` — with `active(LocalDate)`, `active(YearMonth)`, and `latest()` methods. The business rules inside them are correct and stable; we simply need peer-equivalent types in the hexagon domain.

## Goals / Non-Goals

**Goals:**
- Port `EmploymentPeriods` and `RegularWorkingTimes` aggregates (with all query methods) into the hexagon domain package
- Update `ZepProfile` to hold these aggregates instead of raw lists
- Migrate test coverage for both aggregates into the hexagon test package
- Ensure all hexagon code compiles cleanly against the new types (adapters, `SyncUsersService`)
- Remove the `commons-collections4` dependency from `RegularWorkingTimes` (use standard Java)

**Non-Goals:**
- Changing any business logic in the aggregate query methods
- Modifying the legacy `com.gepardec.mega.domain.model` package in any way
- Project-lead role derivation (requires project loading — deferred to a later change)
- Any other sync logic beyond what is needed to compile cleanly

## Decisions

### Decision: Copy logic, do not reuse legacy class
The hexagon aggregate classes are new types in `com.gepardec.mega.hexagon.user.domain.model`. They are not subclasses, wrappers, or imports of the legacy classes. This keeps the hexagon domain fully self-contained with no dependency on the legacy `com.gepardec.mega.domain.model` package, which is an explicit architectural goal.

*Alternative considered*: Type alias / delegate to legacy class. Rejected because it leaks a legacy package dependency into the new domain.

### Decision: ZepProfile holds aggregates, not lists
`ZepProfile.employmentPeriods` becomes `EmploymentPeriods` (the aggregate) and `ZepProfile.regularWorkingTimes` becomes `RegularWorkingTimes`. This is a breaking change to the record constructor but it is isolated: only `ZepEmployeeAdapter` (the one inbound builder of `ZepProfile`) needs updating.

*Alternative considered*: Keep lists in `ZepProfile`, add helper methods alongside. Rejected: mixing aggregate logic into a value object blurs responsibilities.

### Decision: Drop commons-collections4 in RegularWorkingTimes
The only use of `CollectionUtils.isNotEmpty()` in the legacy `RegularWorkingTimes` can be replaced with `regularWorkingTimes != null && !regularWorkingTimes.isEmpty()`. Standard Java is preferred in the hexagon domain to keep the dependency footprint minimal.

## Risks / Trade-offs

- **ZepProfile is a record** → changing field types is a compile-time break for `ZepEmployeeAdapter`. Mitigation: update adapter in the same change, tests will catch regressions.
- **Test parity** → migrated tests must cover the same scenarios as the legacy tests. Mitigation: copy all existing test cases and verify with `mvn test`.

## Migration Plan

1. Add `EmploymentPeriods` and `RegularWorkingTimes` aggregates to `com.gepardec.mega.hexagon.user.domain.model`
2. Update `ZepProfile` to use aggregate types
3. Update `ZepEmployeeAdapter` to wrap lists into aggregates when building `ZepProfile`
4. Verify `SyncUsersService` compiles (it uses `zepProfile.employmentPeriods()` only indirectly through `User` — no changes needed there)
5. Add test classes in `com.gepardec.mega.hexagon.user.domain.model` covering both aggregates
6. Run `mvn test` to confirm no regressions

Rollback: these changes are additive within the hexagon package. The legacy domain is untouched.
