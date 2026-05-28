## Context

The hexagonal backend has an `AuthenticatedActorContext` (request-scoped CDI bean) that resolves the authenticated user on first access. It does so by reading the JWT `email` claim and calling `UserRepository.findByEmail()`. The full `User` domain object is loaded at this point, but currently only three fields are extracted into `AuthenticatedActor` (id, email, roles) — the rest is discarded.

The `MegaRolesAllowedInterceptor` triggers this resolution for every `@MegaRolesAllowed`-annotated endpoint before the handler runs. By the time `GET /users/me` reaches the resource method, the `User` is already in memory.

`MonthEndResource` establishes the precedent: REST adapters inject `AuthenticatedActorContext` directly and read `userId()` from it to pass into use cases.

## Goals / Non-Goals

**Goals:**
- Expose the authenticated user's own profile at `GET /users/me`
- Reuse the `User` already loaded during role resolution — zero additional DB queries
- Follow the established adapter pattern (`MonthEndResource` uses `AuthenticatedActorContext` directly)

**Non-Goals:**
- Introducing a `GetCurrentUserUseCase` — it would add a redundant DB hit with no architectural benefit for this thin query
- Changing authorization or JWT resolution logic
- Exposing employment periods — they are internal domain data not needed by the caller

## Decisions

### Expose `User` from `AuthenticatedActorContext`

**Decision**: Add a `user()` method to `AuthenticatedActorContext` that returns the full `User` cached during `resolveAuthenticatedActor()`.

**Rationale**: The `User` is already loaded. Exposing it avoids a second `findByEmail()` or `findById()` call. The context bean is already in the application layer; it is not a repository and calling it from the adapter does not violate hexagonal layering. `MonthEndResource` already injects this bean directly.

**Alternative considered**: A `GetCurrentUserUseCase` that takes `UserId` and calls `UserRepository.findById()`. Rejected — adds a second indexed DB lookup for no functional gain. Would also require a new inbound port, application service, and test coverage for indirection that adds nothing.

### No new use case or application service

**Decision**: The `UserResource` adapter calls `authenticatedActorContext.user()` and maps the result to a DTO — no intermediate use case.

**Rationale**: A use case is warranted when there is business logic to isolate or orchestrate. Here there is none: the data is already resolved, and the operation is a pure projection. Adding a use case would be structural padding.

### Response field naming

**Decision**: Use `zepUsername` (not `userId`) for the ZEP identifier field. Use `fullName` (not `firstName`/`lastName`) for the name field.

**Rationale**: `userId` is ambiguous — in the legacy layer it means ZEP username, which is confusing. `zepUsername` is explicit. `fullName` matches the existing `ActiveUser` schema and avoids exposing a split that callers have no use for.

## Risks / Trade-offs

- [Coupling] `AuthenticatedActorContext` now serves double duty — security context and profile source. → Acceptable: it already owns the `User`; exposing it is not a new responsibility.
- [Consistency] Future callers of `user()` get a snapshot from request start; any within-request mutation would not be reflected. → Not a concern: `User` is not mutated during a read request.
