## Why

The hexagonal `UserResource` has no equivalent of the legacy `GET /user` endpoint, which the frontend uses to bootstrap the current session — retrieving the authenticated user's own profile (identity, roles, release date). Without this endpoint in the hexagonal API, any frontend migrating away from the legacy layer has no way to fetch "who am I".

## What Changes

- Add `GET /users/me` to the hexagonal User REST API, restricted to `EMPLOYEE` role
- Enrich `AuthenticatedActorContext` to cache and expose the full `User` object (it already loads it during role resolution — this avoids a second DB hit)
- Add a `User` response schema to the OpenAPI spec
- Extend `UserRestMapper` with a mapping from `User` to `UserDto`

## Capabilities

### New Capabilities

_(none — this extends an existing capability)_

### Modified Capabilities

- `user-rest-api`: New requirement — any authenticated employee can retrieve their own profile via `GET /users/me`

## Impact

- `openapi/paths/user.yaml` — new path entry
- `openapi/schemas/user.yaml` — new `User` schema
- `AuthenticatedActorContext` — expose `user()` method (no new DB query)
- `UserResource` (hexagonal) — new `getMe()` handler
- `UserRestMapper` — new `toUserDto(User)` mapping method
