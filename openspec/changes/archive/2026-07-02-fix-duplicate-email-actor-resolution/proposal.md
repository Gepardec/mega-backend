## Why

When an employee returns to the company under a new ZEP account but with the same email address, the user sync creates a duplicate `User` row rather than updating the existing one. At login, `AuthenticatedActorContext` resolves the authenticated actor by email using a non-deterministic first-result query, and consistently returns the older (stale) row — causing the employee to see data from their previous employment.

## What Changes

- **Auth guard**: `findByEmail` in `UserRepositoryAdapter` fails loudly (throws) if more than one `User` is found for an email, preventing silent cross-user data leaks
- **Sync logic**: `SyncUsersService` adds a fallback email lookup for any ZEP employee not found by ZEP username; if a `User` with that email already exists, it is treated as a returning employee and updated in place rather than duplicated
- **DB constraint**: a `UNIQUE` constraint is added to the `users.email` column, enforcing the invariant at the database level as a backstop

## Capabilities

### New Capabilities

_None._

### Modified Capabilities

- `user-sync`: new requirement — when a ZEP employee is not found by ZEP username, the sync must fall back to a lookup by email; if found, the existing `User` is updated (returning employee) rather than a new one being created

## Impact

- `SyncUsersService` — sync logic extended with email fallback
- `UserRepositoryAdapter` — `findByEmail` query hardened against duplicate results
- `UserRepository` (outbound port) — no interface change required; `findByEmail` contract unchanged
- `UserEntity` / Liquibase — new `UNIQUE` constraint on `users.email`
- No REST API changes; no new dependencies
