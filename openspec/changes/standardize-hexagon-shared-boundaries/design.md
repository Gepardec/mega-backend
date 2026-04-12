## Context

The hexagon now spans `user`, `project`, `monthend`, and `worktime`, but several cross-cutting concerns are still implemented independently inside those domains. The current codebase duplicates authenticated actor resolution per domain, mixes direct cross-domain aggregate lookups with domain-specific snapshot projections, and still has a few boundary abstractions that do not clearly express their role.

The external behavior of the new backend is already broadly correct. The problem is architectural drift: the same concern can currently appear as a domain-specific resolver, a repository lookup, a snapshot port, or a legacy boundary import depending on where the feature landed first. That makes the hexagon harder to extend consistently.

Relevant constraints:
- The new backend must continue to follow DDD and hexagonal boundaries.
- Cross-aggregate references should remain explicit and lightweight.
- Existing month-end and worktime API contracts should remain behaviorally stable.
- The change should be incremental and safe to implement in slices.

## Goals / Non-Goals

**Goals:**
- Define one shared pattern for resolving the authenticated actor in hexagon inbound adapters.
- Establish a small shared kernel for stable identity and authorization concepts reused across multiple hexagon domains.
- Keep cross-aggregate persistence references identifier-based and standardize how consuming domains obtain read-model details.
- Clarify the semantics of the cross-domain user identity lookup boundary without expanding the change into a broad naming refactor.

**Non-Goals:**
- Redesign existing REST payloads or business workflows in `monthend` or `worktime`.
- Introduce JPA entity relationships across aggregate boundaries.
- Force `MonthEndEmployee`, `WorkTimeEmployee`, or other domain-local read models into one shared domain model.
- Rewrite the entire legacy security stack in the same change.

## Decisions

### 1. Introduce one shared authenticated actor context for hexagon inbound adapters

The hexagon will expose one shared request-scoped authenticated actor abstraction for inbound adapters. It will resolve authentication once per request and expose the stable identity data needed by hexagon use cases, starting with actor `UserId`, authenticated email, and role information needed at the boundary.

Why this decision:
- It removes duplicated domain-specific REST actor resolvers.
- It makes actor resolution domain-agnostic and reusable for new hexagon domains.
- It keeps use cases explicit because application services will still receive actor identifiers as parameters.

Alternatives considered:
- Keep per-domain resolvers: rejected because it repeats the same resolution logic and exception handling in each domain.
- Produce the full `User` aggregate as a CDI bean: rejected because it hides database access inside request wiring and encourages adapters to depend on a broader object than they need.

### 2. Keep the shared kernel intentionally small

The shared kernel will own stable cross-cutting concepts that are reused across multiple hexagon domains and do not belong to one bounded context. This includes identity value objects, the shared role vocabulary, authenticated actor abstractions, and other truly stable boundary concepts.

Domain-specific projections and reference models will remain local to the consuming domain. `MonthEndUserSnapshot`, `MonthEndProjectSnapshot`, `MonthEndEmployee`, `WorkTimeEmployee`, and similar types stay in their domains unless a future change proves they express the same ubiquitous language and lifecycle.

Why this decision:
- It avoids a new dumping ground for anything “shared”.
- It preserves bounded context language where the same real-world person or project is viewed differently by different domains.

Alternatives considered:
- Create a broad shared domain for user/project references: rejected because it would centralize models that are still shaped by local domain needs.
- Share only utility code and leave identity/authorization split: rejected because actor resolution and role vocabulary are already cross-cutting architectural concerns.

### 3. Keep cross-aggregate persistence references as IDs and use explicit query ports for read models

Cross-aggregate links will continue to be stored as identifiers in persistence models rather than as JPA entity relationships, unless the objects are part of the same aggregate boundary. Consuming domains that need additional context will obtain it through explicit query/read ports and map it into local snapshots or reference models.

Why this decision:
- It keeps aggregate boundaries explicit.
- It avoids accidental lazy-loading and lifecycle coupling across domains.
- It matches the month-end direction, where projections are already explicit and domain-shaped.

Alternatives considered:
- Add JPA `@ManyToOne` or similar mappings across domains: rejected because it reduces lookup boilerplate at the cost of blurrier aggregate boundaries and stronger persistence coupling.
- Use shared reference records everywhere: rejected because it would flatten domain-specific language too early.

### 4. Preserve existing outbound names in this change except for the identity lookup boundary

This change will not perform a broad port or adapter naming refactor. Existing outbound names remain in place unless the current name actively obscures the semantics of the boundary. The single planned exception is renaming `UserLookupPort` to `UserIdentityLookupPort`, because that boundary exists specifically to resolve a `UserId` from a foreign identity and should say so explicitly.

The implementation behind `UserIdentityLookupPort` should also stop using a native SQL query and instead rely on a typed persistence approach aligned with the rest of the hexagon persistence boundary.

Why this decision:
- It keeps the change focused on semantics and boundaries instead of broad mechanical renames.
- It preserves existing names that are already understandable enough for the team.
- It still improves the one lookup boundary whose current name and implementation hide too much intent.

Alternatives considered:
- Perform the full proposed naming cleanup: rejected because it creates too much friction relative to the architectural value right now.
- Keep `UserLookupPort` unchanged: rejected because it underspecifies the purpose of the boundary.
- Keep the native SQL implementation behind the lookup boundary: rejected because it leaks table-level persistence details into a boundary that should be expressed through normal hexagon persistence mechanisms.

### 5. Move shared boundary ownership fully into the hexagon

This change will move shared cross-cutting boundary concerns fully into hexagon-owned packages instead of adding a compatibility bridge around legacy authorization types. Shared concepts that the hexagon truly owns will be implemented directly in shared hexagon code, and legacy imports inside hexagon modules will be removed rather than wrapped.

Why this decision:
- It reduces long-term coupling between the legacy layered architecture and the new hexagon.
- It makes future domains easier to build without copying legacy imports by accident.

Alternatives considered:
- Leave legacy imports in place indefinitely: rejected because it makes the hexagon boundary less coherent over time.
- Introduce a compatibility bridge around legacy authorization types: rejected because it would preserve the split role model and delay boundary ownership instead of resolving it.

## Risks / Trade-offs

- [Risk] A shared actor context could become a dumping ground for unrelated cross-cutting concerns. -> Mitigation: keep the API narrow and limited to identity and authorization data needed by inbound adapters.
- [Risk] A focused identity lookup rename could still trigger avoidable churn if it bleeds into wider naming cleanup. -> Mitigation: explicitly limit this change to `UserIdentityLookupPort` and leave other existing names in place.
- [Risk] Worktime query-port adoption expands the size of the change. -> Mitigation: still implement it in the same change, but keep the work split into mechanical slices so actor centralization, identity lookup cleanup, and query-port extraction can be verified independently.
- [Risk] Shared kernel boundaries can become ambiguous when legacy compatibility is involved. -> Mitigation: introduce explicit shared packages and document temporary compatibility wrappers in code and in the change tasks.

## Migration Plan

1. Introduce the shared authenticated actor abstraction and wire it to existing authentication.
2. Migrate `monthend` and `worktime` inbound adapters from per-domain resolvers to the shared actor context.
3. Extract or define shared kernel ownership for role vocabulary and other truly shared boundary concepts.
4. Rename `UserLookupPort` to `UserIdentityLookupPort` and replace its native-query implementation with a typed persistence approach.
5. Refactor direct cross-domain lookups toward explicit query ports, including the current worktime lookups that still pull foreign aggregates directly.
6. Remove obsolete domain-specific actor resolvers and direct legacy boundary imports from hexagon modules.

Rollback strategy:
- The migration can be rolled back by slice because each step preserves external API contracts and can be reverted independently before removing the old components.

## Open Questions

- None at this time.
