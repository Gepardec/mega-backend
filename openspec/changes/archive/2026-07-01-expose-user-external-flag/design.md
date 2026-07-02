## Context

ZEP usernames follow a naming convention where external employees (contractors, freelancers) are assigned usernames beginning with the letter "e". This convention is currently implicit — no code encodes or exposes it. The frontend needs this classification to adapt its behaviour per user type.

The `ZepUsername` value object lives in the shared domain kernel and is carried by the `User` aggregate. The `GET /users/me` endpoint already returns the `zepUsername` string, but the frontend should not have to re-implement the prefix rule.

## Goals / Non-Goals

**Goals:**
- Encode the external-user classification rule once, in the domain
- Expose `isExternal` as a boolean field on the `GET /users/me` response

**Non-Goals:**
- Exposing `isExternal` on the active users list (`GET /users/active`) — not required by the frontend
- Storing "external" as a persisted flag — it is always derived from the ZEP username
- Changing any business logic based on the external/internal distinction (this change is read-only)

## Decisions

### Rule lives on `ZepUsername`, not on `User`

The predicate `value.startsWith("e")` is a property of the username format itself, not of the user as an aggregate. Placing it on `ZepUsername` keeps it co-located with the data it describes and makes it available anywhere a `ZepUsername` is held — including other bounded contexts — without coupling to the `User` aggregate.

`User` gains a convenience `isExternal()` method that delegates to `zepUsername`, consistent with the pattern of existing aggregate predicates (`isActiveOn`, `isSystemActor`). This is what the REST mapper uses as its mapping source.

**Alternative considered**: Rule only on `User`. Rejected — buries domain knowledge that belongs on the value object, and would force other BCs to resolve the rule through the user BC if they ever needed it.

### `isExternal` is required (non-nullable) in the API response

Every `User` is guaranteed to have a non-null `ZepUsername` (enforced in the `User` constructor for non-system actors), so `isExternal` can always be computed. Making it required avoids unnecessary null-handling on the frontend.

System actors (no `ZepUsername`) are never returned by `GET /users/me` in practice, so the edge case does not arise in the REST layer.

## Risks / Trade-offs

- **Convention drift** → If ZEP changes its username naming convention, the "e" prefix rule becomes stale. Mitigation: the rule is encoded in one place (`ZepUsername`), so a single change fixes it everywhere.
- **Shared kernel change** → Adding behaviour to `ZepUsername` (shared kernel) means all BCs inherit the concept. Acceptable here because the concept is intrinsic to the username format, not to any one BC's domain.
