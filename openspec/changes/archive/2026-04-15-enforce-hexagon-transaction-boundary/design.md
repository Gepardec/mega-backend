## Context

In the hexagon package, `@Transactional` currently appears in two places:

1. **Application services** (`..hexagon..application..`) — 9 classes, all `@ApplicationScoped`. These are the use case implementations (e.g., `CreateMonthEndClarificationService`, `SyncUsersService`). This is correct: the use case defines the unit of work.

2. **Outbound adapters** (`..hexagon..adapter.outbound..`) — 5 repository adapters (e.g., `UserRepositoryAdapter`, `MonthEndTaskRepositoryAdapter`) and 1 lookup adapter (`UserIdentityLookupAdapter`). These implement outbound port interfaces and delegate to Panache repositories. This is incorrect: they are called within the application service's transaction and must not own their own boundary.

Jakarta's default `@Transactional` propagation is `REQUIRED`, meaning adapter calls already join the caller's transaction. Removing the annotation from adapters has no runtime effect today, but the annotations are architecturally wrong and invite future misuse (e.g., accidental `REQUIRES_NEW`).

The existing ArchUnit suite enforces cross-layer dependency rules for the hexagon package but contains no rule about where `@Transactional` is permitted.

## Goals / Non-Goals

**Goals:**
- Remove `@Transactional` from all outbound adapter classes in the hexagon package
- Add an ArchUnit rule that forbids `@Transactional` anywhere in `..hexagon..` outside of `..application..`
- Add an ArchUnit rule that requires `@Transactional` on all classes in `..hexagon..application..` that implement a `*UseCase` interface
- Zero runtime behaviour change

**Non-Goals:**
- Changing transaction semantics or propagation settings on existing services
- Addressing the `PrematureMonthEndPreparationService` cross-use-case call pattern (separate concern)
- Touching legacy `com.gepardec.mega` package

## Decisions

### Decision 1: ArchUnit "must not" rule targets the annotation directly, not the class type

**Choice**: Use `noClasses().that().resideInAPackage("com.gepardec.mega.hexagon..").and().resideOutsideOfPackage("..application..").should().beAnnotatedWith(Transactional.class)` at class level, plus a matching method-level annotation check.

**Alternative considered**: Target only `..adapter.outbound..` packages. Rejected because a narrower rule would silently allow `@Transactional` on inbound adapters or domain classes in the future.

**Rationale**: The rule should express the full intent — the application layer is the *only* permitted home for `@Transactional` within the hexagon.

### Decision 2: "Must" rule targets UseCase implementors, not all application classes

**Choice**: Require `@Transactional` only on classes that implement a `*UseCase` interface, not on every class in `..application..`.

**Alternative considered**: Require it on all `@ApplicationScoped` classes in `..application..`. Rejected because future application-layer helpers (e.g., a mapper or factory) that don't represent use cases shouldn't be forced to carry `@Transactional`.

**Rationale**: The use case interface is the precise contract that marks "this is the unit of work entry point".

### Decision 3: Class-level annotation only (no method-level)

**Choice**: Enforce `@Transactional` at the class level only. Method-level annotations on adapters are equally forbidden by the "must not" rule.

**Rationale**: Class-level annotation covers all methods uniformly, consistent with current practice. Method-level `@Transactional` on an application service method would technically satisfy the "must" rule, but would be harder to enforce consistently — acceptable trade-off given current usage.

## Risks / Trade-offs

- **Panache implicit transactions**: Some Panache operations open their own transaction if none exists. After removing `@Transactional` from adapters, any adapter method called outside an application service transaction (e.g., from a test) will rely on Panache's auto-transaction. This is acceptable — tests should call through the application service or use `@Transactional` at the test level.

- **ArchUnit UseCase interface detection**: The "must" rule relies on the naming convention `*UseCase`. If a future use case interface is named differently, the rule won't catch it. → Mitigation: document the naming convention in the ArchUnit rule comment; the convention is already established across all existing use cases.

## Migration Plan

1. Remove `@Transactional` from 6 outbound adapter classes (mechanical, no logic change)
2. Add the two ArchUnit rules to `ArchitectureTest.java`
3. Run `mvn test -Dtest=ArchitectureTest` — all rules must pass
4. Run `mvn test` — full test suite must pass (no regression)

No deployment steps beyond a normal build and merge. Fully reversible by reverting the commit.
