## Why

The new hexagonal backend (`com.gepardec.mega.hexagon`) has no User concept yet. The legacy `com.gepardec.mega` package assembles User data from ZEP and Personio in a layered, tightly-coupled way — the new architecture needs a clean domain model and sync mechanism built from the ground up using Ports & Adapters.

## What Changes

- Introduce the `User` aggregate root with its own domain UUID, replacing reliance on ZEP's username as identity
- Model ZEP-sourced and Personio-sourced data as separate value objects (`ZepProfile`, `PersonioProfile`) within the aggregate
- Define inbound port `SyncUsersUseCase` and outbound ports `UserRepository`, `ZepEmployeePort`, `PersonioEmployeePort`
- Implement `SyncUsersService` as the application-layer use case that orchestrates the 30-minute sync
- Add `UserSyncScheduler` as the Quarkus `@Scheduled` inbound adapter
- Add outbound adapters that wrap the existing legacy ZEP and Personio REST clients (no HTTP client duplication)
- Add `UserRepositoryAdapter` backed by JPA/Panache with a new `UserEntity`
- Introduce `UserSyncConfig` record to pass office management config into the use case without Quarkus coupling

## Capabilities

### New Capabilities

- `user-aggregate`: Domain model for the User aggregate root, its value objects, and port interfaces
- `user-sync`: Scheduled sync use case that fetches from ZEP and Personio and persists merged User records

### Modified Capabilities

_(none — this is greenfield in the hexagonal package)_

## Impact

- New code exclusively in `com.gepardec.mega.hexagon.user.*`
- Wraps (but does not modify) existing legacy REST clients in `com.gepardec.mega.zep` and `com.gepardec.mega.personio`
- New Liquibase changelog entry for the `hexagon_users` table
- No changes to existing `com.gepardec.mega` packages
- No new external dependencies required
