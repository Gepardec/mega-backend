## 1. Shared actor boundary

- [x] 1.1 Introduce a shared request-scoped authenticated actor abstraction and resolver for hexagon inbound adapters
- [x] 1.2 Add boundary wiring so hexagon REST adapters can consume the shared authenticated actor without changing use case signatures
- [x] 1.3 Replace the month-end and worktime domain-specific REST actor resolvers with the shared authenticated actor context and remove the obsolete resolver classes

## 2. Shared kernel and compatibility cleanup

- [x] 2.1 Define shared ownership for cross-cutting identity and authorization concepts reused by multiple hexagon domains
- [x] 2.2 Replace direct legacy authorization and shared-helper imports in hexagon code with hexagon-owned shared abstractions
- [x] 2.3 Move any reused legacy leak, such as shared billability mapping, into a hexagon-owned shared location when it is still needed across domains

## 3. Cross-domain identity lookup boundary

- [x] 3.1 Rename `UserLookupPort` to `UserIdentityLookupPort` and update its consumers to use the explicit identity lookup boundary
- [x] 3.2 Replace the native-query implementation behind the identity lookup boundary with a typed persistence approach aligned with hexagon persistence conventions
- [x] 3.3 Update dependency injection wiring, imports, and tests impacted by the `UserIdentityLookupPort` change

## 4. Cross-domain query alignment

- [x] 4.1 Keep existing month-end projection port names in place while aligning their semantics with the shared boundary conventions
- [x] 4.2 Introduce explicit worktime query/read ports where worktime currently pulls foreign aggregates only to build local report references
- [x] 4.3 Keep worktime and month-end domain-local snapshots and reference models local while removing obsolete lookup and mapping code replaced by the new query ports

## 5. Verification

- [x] 5.1 Add or update tests that cover shared actor resolution and the `UserIdentityLookupPort` boundary
- [x] 5.2 Run targeted tests for the `monthend`, `worktime`, `user`, and `project` slices impacted by the refactor
- [x] 5.3 Verify that the refactor preserves existing month-end and worktime API behavior while satisfying the new architectural conventions
