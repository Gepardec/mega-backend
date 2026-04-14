## Context

Seven module-specific types across `monthend` and `worktime` represent user and project identity data in slightly different shapes:

| Deleted type | Module | Replaced by |
|---|---|---|
| `MonthEndUserSnapshot` | monthend | `UserRef` |
| `MonthEndEmployee` | monthend | `UserRef` |
| `WorkTimeUserSnapshot` | worktime | `UserRef` |
| `WorkTimeEmployee` | worktime | `UserRef` |
| `MonthEndProject` | monthend | `ProjectRef` |
| `WorkTimeProject` | worktime | `ProjectRef` |
| `WorkTimeProjectSnapshot` | worktime | `ProjectRef` |

`MonthEndProjectSnapshot` survives — it carries `billable` and `leadIds` which are genuinely monthend-specific for task planning. It loses `startDate`/`endDate` because activeness filtering moves to the adapter.

`FullName` and `ZepUsername` are value objects currently owned by the `user` module but used in cross-module types. They move to `shared/domain/model/` so `UserRef` can carry them without creating a cross-module domain dependency.

Activeness filtering today happens in `GenerateMonthEndTasksService` after `findAll()`:
```java
monthEndUserSnapshotPort.findAll().stream()
    .filter(user -> user.isActiveIn(month))   // application service concern — wrong layer
```

This behaviour moves into the outbound adapter implementations.

## Goals / Non-Goals

**Goals:**
- Introduce `UserRef` and `ProjectRef` in `shared/domain/model/`
- Move `FullName` and `ZepUsername` to shared
- Delete the seven redundant projection types
- Change port signatures to `findActiveIn(YearMonth)` and push activeness filtering into adapters
- Trim `MonthEndProjectSnapshot` — remove `startDate`/`endDate`

**Non-Goals:**
- Moving the full `User` or `Project` aggregate to shared
- Changing any REST API contract or DTO structure
- Touching the legacy `com.gepardec.mega` package
- Addressing `PrematureMonthEndPreparationService` cross-use-case concerns (separate change)

## Decisions

### Decision 1: UserRef carries ZepUsername even where not displayed

`ZepUsername` is included in `UserRef` even though it's not always needed in REST responses. REST adapters that don't need it simply don't map it to their DTO.

**Alternative**: Two types — a minimal `UserRef { id, fullName }` and a fuller `UserSnapshot { id, fullName, zepUsername }`. Rejected because maintaining two types re-introduces the duplication we're eliminating. The marginal cost of an unused field is negligible.

### Decision 2: ProjectRef carries zepId

`ProjectRef` includes `zepId` to support the worktime module's need to cross-reference ZEP assignments. REST adapters that expose only name/id simply omit it.

**Alternative**: Separate `ProjectRef { id, name }` and `ProjectSnapshot { id, zepId, name }`. Rejected for the same reason as Decision 1.

### Decision 3: Activeness filtering in the adapter, not a shared domain service

The `findActiveIn(YearMonth)` port contract delegates the filtering implementation to each adapter. The adapter may push it to the database query or perform it in-memory before returning — the port contract does not prescribe which. This keeps the port simple and testable without mandating a specific persistence strategy.

**Alternative**: A shared `UserActivity` domain service that both modules call. Rejected — it creates a domain dependency on `EmploymentPeriods` in modules that shouldn't know about it.

### Decision 4: MonthEndProjectSnapshot loses isActiveIn() — filtering fully adapter-owned

Removing `startDate`/`endDate` from `MonthEndProjectSnapshot` makes it impossible to accidentally reintroduce in-memory activeness filtering in the application service. The constraint is structural rather than just a convention.

## Risks / Trade-offs

- **Widespread import churn** — every file using the deleted types needs updating. Mechanical, but touches many files across monthend and worktime. → Mitigation: do the value-object moves (`FullName`, `ZepUsername`) first so the compiler guides the remaining changes.

- **Adapter activeness logic must be correct** — if the outbound adapter implementation misses edge cases (e.g., open-ended employment periods, null endDate on projects), the application service previously acted as a safety net. → Mitigation: the adapter implementations must have their own unit tests covering the edge cases; the existing `EmploymentPeriods.isActive()` logic can be reused directly.

## Migration Plan

1. Move `FullName` and `ZepUsername` to `shared/domain/model/`; fix all imports (compiler-guided)
2. Add `UserRef` and `ProjectRef` to `shared/domain/model/`
3. Update port interfaces: `findAll()` → `findActiveIn(YearMonth)`, return type → `UserRef` / `List<MonthEndProjectSnapshot>` (trimmed)
4. Update adapter implementations: add activeness filtering logic
5. Update `MonthEndProjectSnapshot`: remove `startDate`, `endDate`, `isActiveIn()`
6. Update `MonthEndUserSnapshot` → remove, replace usages with `UserRef`; same for the six other deleted types
7. Update application services: remove in-memory activeness filters
8. Simplify `MonthEndWorklistMapper`: remove `toProject()` / `toSubjectEmployee()`; update `MonthEndWorklistItem` field types
9. Run `mvn test` — full suite must pass
