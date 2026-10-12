## Why

The `com.gepardec.mega.hexagon` package spans 5 subdomains and 169 production classes, but only 2 ArchUnit rules currently guard it — both buried inside the legacy-focused `ArchitectureTest`. Layer isolation, domain purity, and structural conventions are enforced only by code review, which doesn't scale as the hexagon grows.

## What Changes

- New `HexagonalArchitectureTest` class with 17 ArchUnit rules covering all hexagonal structure guarantees
- 2 existing hexagon-related rules removed from `ArchitectureTest.java` and consolidated into the new class
- New `hexagon-layer-constraints` spec formalising the structural rules that will be enforced

## Capabilities

### New Capabilities

- `hexagon-layer-constraints`: Structural rules for the hexagon package — layer dependency direction, domain model purity, port naming conventions, adapter placement constraints, and application service conventions — all enforced by ArchUnit

### Modified Capabilities

_(none — transaction boundary requirements already captured in `hexagon-transaction-boundary`; no spec-level requirement changes)_

## Impact

- **New file**: `src/test/java/com/gepardec/mega/hexagon/HexagonalArchitectureTest.java`
- **Modified file**: `src/test/java/com/gepardec/mega/ArchitectureTest.java` — 2 hexagon rules removed
- **No production code changes**
- **No new dependencies** — ArchUnit 1.3.0 already on the test classpath
