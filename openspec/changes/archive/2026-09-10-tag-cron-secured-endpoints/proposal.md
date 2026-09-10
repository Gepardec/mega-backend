## Why

The internal generation endpoint is secured by the `mega-cron` client-credentials scheme and can never be called from a browser session, but the contract grouped it with the actor-facing monthend operations. Consumers that generate clients from the contract had no way to tell the two apart, so the frontend excluded the endpoint by hardcoding its path — a list it had to keep in sync by hand, and one that silently goes stale when a new machine-to-machine endpoint is added.

## What Changes

- Group monthend endpoints secured by the internal client-credentials scheme under a contract tag of their own, distinct from the tags carried by actor-facing endpoints.
- `POST /monthend/{month}/generate` moves off the actor-facing monthend tag onto that operational tag. Its path, request, response, and role requirements are unchanged.
- Consumers can now exclude every machine-to-machine monthend operation by tag alone. The frontend drops its path-based exclusion in favour of a tag filter (tracked separately in `mega-frontend-v2`).

Not **BREAKING** for HTTP callers: no path, payload, or authorization behaviour changes. It is a source-compatible break for consumers that generate code grouped by tag, which will see the operation move to a different generated grouping.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `monthend-rest-api`: gains a requirement that monthend endpoints secured by the internal client-credentials scheme are grouped under a contract tag no actor-facing endpoint carries, so consumers can identify and exclude them by tag. The existing requirements, including the one covering the generation endpoint itself, are unchanged.

## Impact

- The canonical OpenAPI document: a new operational tag, and a retag of the generation operation.
- Server interfaces generated per tag, and therefore the monthend REST adapter layer, which splits the operational endpoint into its own adapter class. No change to routing or security enforcement.
- `mega-frontend-v2`, which switches its generated-client exclusion from a path list to a tag filter.
