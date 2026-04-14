## Why

`@Transactional` is currently applied to both application services (correct) and outbound port adapters (incorrect) in the hexagon package, creating ambiguity about who owns the transaction boundary and misleading developers into thinking adapters manage their own consistency scope. Establishing a clear, enforced rule eliminates this confusion before the hexagon codebase grows further.

## What Changes

- Remove `@Transactional` from all outbound adapter classes in `com.gepardec.mega.hexagon..adapter.outbound..`
- Add two ArchUnit rules to `ArchitectureTest.java`:
  - **Must not**: No class in the hexagon package outside of `..application..` may be annotated with `@Transactional`
  - **Must**: Every class in `..hexagon..application..` that implements a `*UseCase` interface must be annotated with `@Transactional`

## Capabilities

### New Capabilities

- `hexagon-transaction-boundary`: Defines where `@Transactional` is permitted within the hexagon package and enforces the rule via ArchUnit

### Modified Capabilities

*(none — no existing spec-level requirements change; this change introduces an architectural constraint, not a behavioural one)*

## Impact

- 6 outbound adapter classes lose their `@Transactional` annotation (no runtime behaviour change — they already participate in the application service's transaction via Jakarta's default `REQUIRED` propagation)
- `ArchitectureTest.java` gains two new rules
- No API contract changes, no database schema changes, no impact on the legacy `com.gepardec.mega` package
