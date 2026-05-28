## Why

The new hexagon backend has started to duplicate request actor resolution and cross-domain reference lookups as more domains are added. That makes the shared model harder to recognize, hides which concerns are truly domain-local, and makes the architecture feel less intentional than the underlying design goals.

## What Changes

- Introduce one shared hexagon-side authenticated actor context for inbound adapters instead of per-domain REST actor resolvers.
- Define a small shared kernel for stable cross-cutting concepts such as authenticated actor identity, authorization concepts, and core identity value objects.
- Preserve cross-aggregate persistence references as IDs instead of JPA entity relationships, and standardize domain-specific read models around explicit query ports and local snapshots.
- Clarify the cross-domain user identity lookup boundary by renaming `UserLookupPort` to `UserIdentityLookupPort` and aligning its implementation with normal hexagon persistence boundaries.
- Remove legacy boundary leaks from the hexagon where shared hexagon concerns should own the responsibility instead.

## Capabilities

### New Capabilities
- `hexagon-boundary-conventions`: Defines the architectural rules for shared authenticated actor resolution, shared kernel ownership, cross-domain reference/query patterns, and cross-domain identity lookup in the new hexagon backend.

### Modified Capabilities
- None.

## Impact

- Affects hexagon inbound REST adapters in `monthend` and `worktime`.
- Affects shared identity and authorization handling across `user`, `project`, `monthend`, and `worktime`.
- Affects the cross-domain identity lookup boundary used during lead reconciliation and similar cross-domain lookups.
- Affects how cross-domain projections and reference models are introduced for future hexagon domains.
