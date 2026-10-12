## Context

`AuthenticatedActorContext` resolves the authenticated actor by looking up the email claim from the Keycloak-issued JWT (backed by Google as the IDP) in the local `users` table. Keycloak uses Google accounts, where email is the stable, unchanging identity. However, the `users.email` column has no `UNIQUE` constraint, and the repository uses `firstResultOptional()` — which is non-deterministic when multiple rows share an email.

The duplicate arises in the sync path: `SyncUsersService` matches incoming ZEP employees to existing `User` rows by `zepUsername`. When an employee returns under a new ZEP account (new `zepUsername`, same email), the sync finds no match and calls `User.create()`, producing a second row for the same person. The DB then contains two rows for the same email, and `firstResultOptional()` consistently returns the older row (heap insertion order), so the returning employee sees data from their previous employment.

## Goals / Non-Goals

**Goals:**
- Prevent the auth layer from silently returning the wrong user when duplicate emails are present
- Fix the sync path so that returning employees (same email, new ZEP username) update the existing `User` instead of creating a new one
- Enforce email uniqueness at the database level

**Non-Goals:**
- Storing Keycloak subject (`sub`) claims or building an IDP identity mapping — email is stable for this system (Google accounts, never change)
- Preserving historical ZEP usernames — only the current active ZEP account is tracked
- Automated cleanup of existing duplicate rows — this is a one-time manual hotfix

## Decisions

### Decision: Two-phase lookup in sync (ZEP username first, email fallback)

The existing batch lookup by `zepUsername` is efficient and covers the common case. A separate email fallback only runs for employees not found by username — expected to be rare (only returning employees). This avoids touching the happy-path batch query.

Alternative considered: email-primary lookup for all employees. Rejected: it would require an individual query per employee on every sync run, eliminating the batch efficiency for no benefit in the common case.

### Decision: Fail loudly in `findByEmail` if duplicates are present

If multiple `User` rows share an email, `findByEmail` will throw rather than silently returning one. A `ForbiddenException` at login is far less harmful than a silent data leak. This also makes violations observable immediately rather than after an employee reports wrong data.

Alternative considered: return the most-recently-created row. Rejected: this rewards the violation with working behaviour, masking bugs and delaying detection.

### Decision: `UNIQUE` constraint added via Liquibase migration

The constraint enforces the invariant at the database level — independent of application logic. This is the final backstop; no application bug can create a duplicate and leave it invisible.

The migration must be applied **after** any pre-existing duplicate rows have been manually removed from the database. Applying it before cleanup will fail with a constraint violation.

## Risks / Trade-offs

- **Migration ordering**: the Liquibase `UNIQUE` constraint changeset will fail if duplicate rows still exist in the database at deployment time. The manual data hotfix (removing the stale duplicate row) must be applied first. → Deploy the data hotfix to production before releasing this change.
- **Email fallback query is unbatched**: the fallback `findByEmail` runs once per "not found by username" employee. In the common case (no returning employees in a sync cycle) this is zero extra queries. In the exceptional case it is one query per returning employee, which is acceptable.
- **`withSyncedZepData` replaces employment periods**: when a returning employee's existing `User` is updated via the email fallback, `withSyncedZepData` replaces the stored employment periods with those from the new ZEP account. The old employment period is lost from local persistence. This is acceptable because ZEP is the authoritative source for employment periods, and the new account carries only the new employment data.

## Migration Plan

1. **Before deployment**: manually delete the stale duplicate `User` row(s) from the production database. The affected employee's stale row (old `zepUsername`) can be identified by checking which row has the older `id` or which `zepUsername` is no longer returned by ZEP.
2. **Deploy**: release the code changes (auth guard + sync fallback).
3. **Apply migration**: Liquibase will run the `UNIQUE` constraint changeset on startup. This will succeed now that the duplicate row has been removed.
4. **Trigger sync**: the next scheduled sync run will verify that the returning employee is correctly matched by email and not re-duplicated.

**Rollback**: removing the `UNIQUE` constraint is possible via a new Liquibase changeset but should not be necessary — the constraint formalises an invariant that was always intended to hold.
