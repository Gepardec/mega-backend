## Why

The hexagonal domain's `ZepProfile` currently holds plain `List<EmploymentPeriod>` and `List<RegularWorkingTime>` records with no business logic. The legacy domain already has proven aggregate classes — `EmploymentPeriods` and `RegularWorkingTimes` — that encapsulate meaningful temporal query logic (`active()`, `latest()`). Moving these into the hexagon package gives the new domain its own self-contained aggregates and unlocks richer sync logic (e.g. deriving active status from employment periods rather than passing raw lists around).

## What Changes

- Introduce `EmploymentPeriods` aggregate (with `active(LocalDate)`, `active(YearMonth)`, `latest()`, `empty()`) into `com.gepardec.mega.hexagon.user.domain.model`
- Introduce `RegularWorkingTimes` aggregate (with `active(LocalDate)`, `active(YearMonth)`, `latest()`, `empty()`) into `com.gepardec.mega.hexagon.user.domain.model`
- Replace `List<EmploymentPeriod>` with `EmploymentPeriods` in `ZepProfile`
- Replace `List<RegularWorkingTime>` with `RegularWorkingTimes` in `ZepProfile`
- Migrate full test suites for both aggregates into the hexagon test package — adapted to the new package, no legacy imports
- Adapt `SyncUsersService` and any adapters that construct `ZepProfile` to use the new aggregate types
- Remove `commons-collections4` dependency from `RegularWorkingTimes` (use standard `List.isEmpty()` instead)

## Capabilities

### New Capabilities
- `employment-periods-aggregate`: `EmploymentPeriods` aggregate in the hexagon domain with temporal query methods
- `regular-working-times-aggregate`: `RegularWorkingTimes` aggregate in the hexagon domain with temporal query methods

### Modified Capabilities
- `user-aggregate`: `ZepProfile` value object fields change from raw lists to aggregate types
- `user-sync`: `SyncUsersService` and ZEP/Personio adapters updated to construct aggregate types

## Impact

- `com.gepardec.mega.hexagon.user.domain.model.ZepProfile` — field types change (**BREAKING** for any adapter constructing it)
- `com.gepardec.mega.hexagon.user.adapter.outbound.ZepEmployeeAdapter` — must wrap lists into aggregates when building `ZepProfile`
- `com.gepardec.mega.hexagon.user.application.SyncUsersService` — no logic changes, but compiles against new aggregate types
- No changes to the legacy `com.gepardec.mega.domain.model` package or its tests
