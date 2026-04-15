## Context

The `com.gepardec.mega.hexagon` package is the active development target. It contains 5 subdomains (monthend, user, project, worktime, shared) and 169 production classes. Currently two ArchUnit rules covering hexagon-specific concerns live in `ArchitectureTest` alongside legacy-layer rules:

- `hexagonClassesOutsideApplicationShouldNotBeTransactional`
- `hexagonUseCaseImplementationsShouldBeTransactional`

There is no dedicated test class for hexagon structural rules, and no rules enforcing layer dependency direction, domain purity, port naming, or adapter placement. ArchUnit 1.3.0 is already on the test classpath.

## Goals / Non-Goals

**Goals:**
- Create `HexagonalArchitectureTest` as the single home for all hexagon structural rules
- Enforce the five layer dependency directions that define hexagonal architecture
- Guard domain model purity (no JPA/CDI framework leakage, immutability via records)
- Enforce port and adapter naming conventions
- Consolidate the two existing hexagon rules from `ArchitectureTest`

**Non-Goals:**
- Testing business logic or runtime behaviour
- Enforcing conventions inside legacy `com.gepardec.mega` packages
- Constraining test class naming or test package structure

## Decisions

### Decision: Dedicated test class, not additions to `ArchitectureTest`

`ArchitectureTest` imports `com.gepardec.mega` (the full package tree) and focuses on legacy layer rules. Mixing hexagon rules there adds noise and makes the class harder to maintain as the hexagon grows. A dedicated `HexagonalArchitectureTest` scoped to `com.gepardec.mega.hexagon` is faster (smaller import scope) and self-contained.

_Alternative considered_: Adding all rules to `ArchitectureTest`. Rejected because it conflates two architectures in one file and the `@BeforeAll` import would need to expand or duplicate.

### Decision: Use `Record.class` assignability to enforce domain model immutability

Java records implicitly extend `java.lang.Record`. `beAssignableTo(Record.class)` in ArchUnit checks this cleanly without reflection hacks. Enums are excluded by `areNotEnums()` since the domain model layer legitimately contains status and type enums that cannot be records.

_Alternative considered_: Custom `ArchCondition` using `JavaClass.isRecord()`. Works equally well but `beAssignableTo` is more idiomatic with the existing rule-definition DSL.

### Decision: Package patterns use `..hexagon..` wildcards

Rules are written against `..hexagon..domain..`, `..hexagon..application..`, `..hexagon..adapter..inbound..`, and `..hexagon..adapter..outbound..` using ArchUnit's double-dot wildcard. This covers all 5 subdomains uniformly without listing each by name, and remains correct when a sixth subdomain is added.

### Decision: IMPLEMENTS_USE_CASE_INTERFACE reuses the predicate from `ArchitectureTest`

Both the existing test and the new class need the same predicate that identifies non-interface classes implementing a `*UseCase`-named interface. The predicate will be duplicated (copy-pasted) rather than extracted to a shared helper, keeping both test classes self-contained. Test utilities should not depend on each other.

### Decision: 17 rules in 5 groups

| Group | Count | Rationale |
|---|---|---|
| Layer dependency | 5 | Core hexagonal invariant — most critical |
| Domain purity | 2 | Prevents JPA/CDI leaking into domain records |
| Port conventions | 3 | Keeps port packages structurally consistent |
| Application services | 2 | Complements existing `@Transactional` rules |
| Adapter placement | 3 | Prevents Panache/JPA types escaping the adapter layer |
| Transaction (moved) | 2 | Consolidated from `ArchitectureTest` |

## Risks / Trade-offs

**[Risk] A rule fails on legitimate new code** → Most likely candidate is `domainModelsMustBeRecordsOrEnums` if a future domain model genuinely needs to be a class (e.g., a mutable builder). Mitigation: the rule is easy to adjust; the failure message will be obvious.

**[Risk] `applicationMustNotDependOnAdapterLayer` breaks MapStruct usage** → If a mapper in `application/` imports a type from `adapter/outbound/`, the rule will fail. Mitigation: check current mapper placement during implementation; mappers should be co-located with the adapter they serve, not in `application/`.

**[Risk] Moving the two transaction rules causes test-run ordering issues** → ArchUnit rules are stateless and order-independent. No risk.

**[Trade-off] 17 rules adds maintenance overhead** → Each structural convention now has a test that will fail when violated. This is intentional — the overhead is the cost of keeping the architecture honest.
