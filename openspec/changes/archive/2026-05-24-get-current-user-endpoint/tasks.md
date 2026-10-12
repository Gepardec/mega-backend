## 1. OpenAPI Spec

- [x] 1.1 Add `User` schema to `openapi/schemas/user.yaml` with fields: `id` (uuid), `email`, `fullName`, `zepUsername`, `releaseDate` (nullable date), `roles` (string array), `personioId` (nullable integer)
- [x] 1.2 Add `GET /users/me` path entry to `openapi/paths/user.yaml` referencing the `User` schema

## 2. AuthenticatedActorContext

- [x] 2.1 Cache the full `User` object in `AuthenticatedActorContext` as the single source for authenticated context data
- [x] 2.2 Expose a `user()` method on `AuthenticatedActorContext` that returns the cached `User`
- [x] 2.3 Update `AuthenticatedActorContextTest` to cover the new `user()` method

## 3. REST Adapter

- [x] 3.1 Regenerate the OpenAPI-generated sources (run `mvn generate-sources`) to produce `getCurrentUser()` on `UserApi` and `UserDto`
- [x] 3.2 Add `toUserDto(User user)` to `UserRestMapper` using `@Mapping` annotations
- [x] 3.3 Inject `AuthenticatedActorContext` into `UserResource` and implement `getCurrentUser()` — call `authenticatedActorContext.user()`, map to `UserDto`, return `200 OK`
- [x] 3.4 Annotate `getCurrentUser()` with `@MegaRolesAllowed(Role.EMPLOYEE)`

## 4. Tests

- [x] 4.1 Add unit tests for the new `UserRestMapper.toUserDto()` mapping
- [x] 4.2 Add tests to `UserResourceTest` covering: successful `200` response, `403` for missing role, nullable `releaseDate`, nullable `personioId`
