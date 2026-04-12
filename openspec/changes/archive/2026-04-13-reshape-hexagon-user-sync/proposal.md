## Why

The current hexagon user sync persists provider-shaped JSON blobs, derives office-management roles from the wrong identifier, and models the user domain differently from the other hexagon domains. This makes the local user model heavier than necessary, hides the real ownership boundaries, and makes future user-related use cases harder to evolve.

## What Changes

- Redesign the hexagon `User` aggregate around local identity and authorization data instead of persisted `ZepProfile` and `PersonioProfile` blobs.
- Normalize user roles and employment periods into explicit persistence structures instead of storing them as serialized JSON on `hexagon_users`.
- Derive user activity from persisted employment periods instead of persisting a redundant active/inactive status.
- Change user sync so it processes all ZEP employees with a non-null email, persists essential local user data, and stores Personio only as a stable `personioId` reference.
- Assign the `OFFICE_MANAGEMENT` role by configured email address instead of ZEP username.
- Move the user-sync use case onto the same CDI, transaction, and logging style used by the existing monthend and worktime application services.
- Replace the mutable class-based `User` model with an immutable record-oriented model aligned with the rest of the hexagon domain.
- Introduce on-demand provider detail lookups for data that should not live in the local user store, including Personio-only details and regular working times.

## Capabilities

### New Capabilities
- `user-provider-detail-lookup`: Defines how hexagon use cases fetch provider-owned user details on demand instead of persisting full provider snapshots in the local user aggregate.

### Modified Capabilities
- `user-aggregate`: Redefines the local shape of the `User` aggregate, its persisted references, derived activity semantics, and role storage.
- `user-sync`: Changes sync input filtering, role assignment, Personio handling, persistence responsibilities, and application-service boundaries.

## Impact

- Affects hexagon user domain models, sync use cases, repository ports, persistence adapters, Liquibase changelogs, and scheduler wiring.
- Affects monthend and worktime read/query adapters that currently navigate through `User.zepProfile`.
- Affects Personio and ZEP outbound ports by moving provider-only fields behind explicit query capabilities instead of aggregate persistence.
- Requires migration of `hexagon_users` data from JSON blob columns to normalized tables and reference columns.
