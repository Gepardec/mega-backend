## 1. Remove @Transactional from outbound adapters

- [x] 1.1 Remove `@Transactional` from `user/adapter/outbound/UserRepositoryAdapter`
- [x] 1.2 Remove `@Transactional` from `project/adapter/outbound/ProjectRepositoryAdapter`
- [x] 1.3 Remove `@Transactional` from `project/adapter/outbound/UserIdentityLookupAdapter`
- [x] 1.4 Remove `@Transactional` from `monthend/adapter/outbound/MonthEndTaskRepositoryAdapter`
- [x] 1.5 Remove `@Transactional` from `monthend/adapter/outbound/MonthEndClarificationRepositoryAdapter`
- [x] 1.6 Search for any remaining `@Transactional` usages in `..hexagon..adapter..` and remove them

## 2. Add ArchUnit enforcement rules

- [x] 2.1 Add rule to `ArchitectureTest.java`: no class in `com.gepardec.mega.hexagon..` outside of `..application..` may be annotated with `@Transactional` (class or method level)
- [x] 2.2 Add rule to `ArchitectureTest.java`: every class in `..hexagon..application..` that implements a `*UseCase` interface must be annotated with `@Transactional`

## 3. Verify

- [x] 3.1 Run `mvn test -Dtest=ArchitectureTest` — both new rules must pass
- [x] 3.2 Run `mvn test` — full test suite must pass with no regressions
