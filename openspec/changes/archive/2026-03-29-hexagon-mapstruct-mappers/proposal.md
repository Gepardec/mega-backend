## Why

Manual mapping code in the hexagonal adapters is verbose, error-prone, and inconsistent with the project convention of using MapStruct for all object mapping. Replacing it with MapStruct mappers reduces boilerplate and makes the mapping layer declarative and testable.

## What Changes

- Extract `UserMapper` from `UserRepositoryAdapter` — MapStruct-generated `toEntity(User)`, hand-written `toDomain(UserEntity)` calling `User.reconstitute()`
- Extract `ProjectMapper` from `ProjectRepositoryAdapter` — same pattern as `UserMapper`
- Extract `ZepProjectMapper` from `ZepProjectAdapter` — fully generated `ZepProject → ZepProjectProfile` record mapping
- Extract `ZepEmployeeMapper` from `ZepEmployeeAdapter` — multi-source `toZepProfile(ZepEmployee, EmploymentPeriods, RegularWorkingTimes) → ZepProfile`; adapter retains IO orchestration and `toRegularWorkingTime()` logic
- `PersonioEmployeeAdapter` is unchanged (null-safe accessor chains + side-effecting `fetchVacationBalance()` make MapStruct gain negligible)
- Legacy `com.gepardec.mega` package is untouched

## Capabilities

### New Capabilities

- None — this is a refactor. No new domain capabilities are introduced.

### Modified Capabilities

- `user-aggregate`: implementation detail only — `UserMapper` is introduced as the mapping strategy; no requirement changes
- `project-aggregate`: implementation detail only — `ProjectMapper` is introduced as the mapping strategy; no requirement changes
- `user-sync`: implementation detail only — `ZepEmployeeMapper` replaces inline mapping in `ZepEmployeeAdapter`; no requirement changes
- `project-sync`: implementation detail only — `ZepProjectMapper` replaces inline mapping in `ZepProjectAdapter`; no requirement changes

## Impact

- **Modified files**: `UserRepositoryAdapter`, `ProjectRepositoryAdapter`, `ZepEmployeeAdapter`, `ZepProjectAdapter`
- **New files**: `UserMapper`, `ProjectMapper`, `ZepEmployeeMapper`, `ZepProjectMapper` (all in their respective `adapter/outbound/` packages)
- **No API changes** — all changes are internal to the adapter layer
- **No behavior changes** — mapping logic is preserved, just moved to MapStruct mappers
- **Dependencies**: MapStruct is already on the classpath (set up in a prior commit)
