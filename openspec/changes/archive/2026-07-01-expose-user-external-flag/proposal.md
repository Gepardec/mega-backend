## Why

The frontend needs to distinguish external users from internal ones so it can adapt behaviour accordingly. This classification is already implicit in ZEP usernames (external usernames start with "e") but is not exposed through the API, forcing the frontend to duplicate that naming convention logic.

## What Changes

- `ZepUsername` value object gains an `isExternal()` predicate derived from the username prefix
- `User` aggregate gains an `isExternal()` convenience method delegating to `ZepUsername`
- The `GET /users/me` response (`UserDto`) gains a required boolean field `isExternal`
- The OpenAPI schema for `User` is updated accordingly

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

- `user-rest-api`: the `GET /users/me` response now includes the `isExternal` boolean field
- `user-aggregate`: the `User` aggregate gains an `isExternal()` predicate derived from the ZEP username prefix

## Impact

- `ZepUsername` (shared domain value object)
- `User` (user domain aggregate)
- OpenAPI schema (`User` object in `schemas/user.yaml`)
- `UserRestMapper` (REST adapter mapping)
- Frontend consumers of `GET /users/me`
