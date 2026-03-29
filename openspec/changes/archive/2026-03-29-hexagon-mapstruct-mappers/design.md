## Context

The hexagonal package (`com.gepardec.mega.hexagon`) contains four adapters with hand-written mapping code between external types (JPA entities, ZEP DTOs, Personio DTOs) and domain aggregates/value objects. MapStruct is already on the classpath (set up previously). CLAUDE.md mandates MapStruct for all mapping and requires mappers to be co-located with the adapter they serve.

The two aggregate adapters (`UserRepositoryAdapter`, `ProjectRepositoryAdapter`) use a `reconstitute()` static factory on their domain aggregates — the domain constructors are private and not accessible to outside packages.

`ZepEmployeeAdapter.toZepProfile()` is not a pure transformation: it makes two additional API calls (`employmentPeriodService`, `regularWorkingTimesService`) to load data not present on `ZepEmployee`. The `toRegularWorkingTime()` helper builds an `EnumMap<DayOfWeek, Duration>` from individual named fields — not expressible as a declarative MapStruct mapping.

## Goals / Non-Goals

**Goals:**
- Replace all hand-written mapping in hexagonal adapters with MapStruct mappers
- Co-locate each mapper with its adapter in the `adapter/outbound/` package
- Preserve all existing mapping behavior exactly
- Keep domain aggregate constructors private and `reconstitute()` factories intact

**Non-Goals:**
- Changing `PersonioEmployeeAdapter` (IO + null-safe accessor chains make MapStruct gain negligible)
- Touching legacy `com.gepardec.mega` package
- Introducing dedicated mappers for simple value objects (`Email`, `UserId`, `FullName`, etc.)
- Changing domain model structure

## Decisions

### Decision 1: Abstract class over interface for aggregate mappers

**Choice**: Use `abstract class` for `UserMapper` and `ProjectMapper`.

**Rationale**: The `toDomain()` direction must call `User.reconstitute()` / `Project.reconstitute()` because the domain constructors are private. MapStruct cannot generate this. An abstract class allows a concrete `toDomain()` method alongside abstract generated methods. An interface with `default` methods would also work but abstract classes are slightly more idiomatic when mixing generated and hand-written methods.

**Alternative considered**: Make domain constructors public/package-private and let MapStruct generate `toDomain()` fully. Rejected — it would allow bypassing domain factories from anywhere, breaking the DDD invariant that aggregate construction is controlled.

### Decision 2: Keep `reconstitute()` on domain aggregates

**Choice**: `User.reconstitute()` and `Project.reconstitute()` remain unchanged.

**Rationale**: `reconstitute()` is meaningful DDD vocabulary — it signals "rebuilding an aggregate from persisted state." Removing it to accommodate MapStruct inverts the dependency (infrastructure concern leaks into domain design). The mapper calls `reconstitute()` explicitly in its concrete `toDomain()` method.

### Decision 3: Multi-source mapper for `ZepEmployeeMapper`

**Choice**: `ZepEmployeeMapper.toZepProfile(ZepEmployee, EmploymentPeriods, RegularWorkingTimes) → ZepProfile`

**Rationale**: `ZepEmployeeAdapter` fetches `EmploymentPeriods` and `RegularWorkingTimes` via separate API calls — these are not derivable from `ZepEmployee` alone. The adapter retains all IO orchestration and constructs the aggregates (including `toRegularWorkingTime()`), then delegates the pure transformation to the mapper.

MapStruct supports multi-source mapping natively via multiple method parameters.

### Decision 4: `toRegularWorkingTime()` stays in the adapter

**Choice**: The `toRegularWorkingTime()` method (builds `EnumMap<DayOfWeek, Duration>` from 7 individual `Double` fields) is not moved to a mapper.

**Rationale**: This transformation cannot be expressed declaratively — it requires imperative logic to build the map entry by entry. Moving it to a MapStruct `default` method would provide no benefit over the current private method. It stays as a private method on `ZepEmployeeAdapter`.

### Decision 5: Value objects constructed inline via expressions

**Choice**: `UserId.of()`, `Email.of()`, `FullName.of()`, `ProjectId.of()` are constructed inline in mapper `@Mapping` expressions or in the hand-written `toDomain()` body — no dedicated value object mappers.

**Rationale**: These are one-liner factory calls. A dedicated `EmailMapper` wrapping `Email.of(string)` adds navigation overhead with zero logic reduction.

## Risks / Trade-offs

- **`ZepProfile` is a record with `EmploymentPeriods` / `RegularWorkingTimes` fields** → MapStruct must use canonical constructor injection for records. Both fields are passed as mapper parameters (multi-source), so no sub-mapping needed. Low risk.
- **Enum string conversion for `Set<Role>`** → MapStruct needs a named helper method for `Set<String> → Set<Role>` (and reverse). Straightforward, but must be explicitly tested. Low risk.
- **Null-safe date conversions from `java.sql.Date/Timestamp → LocalDate`** → `ZepProjectMapper` needs an expression or named method for `.toLocalDate()` with null guard. Already handled manually today; same logic moves to the mapper.
- **Behavior preservation** → No tests currently exist for the individual mapping methods. Existing integration tests exercise the full adapter stack and will catch regressions.

## Migration Plan

1. Create `UserMapper` → update `UserRepositoryAdapter` to inject and use it → verify tests pass
2. Create `ProjectMapper` → update `ProjectRepositoryAdapter` → verify tests pass
3. Create `ZepProjectMapper` → update `ZepProjectAdapter` → verify tests pass
4. Create `ZepEmployeeMapper` → update `ZepEmployeeAdapter` → verify tests pass

Each mapper can be introduced and wired independently. No database migrations, no API changes, no deployment coordination required. Rollback is reverting the relevant files.
