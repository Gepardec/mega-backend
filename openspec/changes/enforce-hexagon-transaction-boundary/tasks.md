## 1. Remove @Transactional from outbound adapters

- [ ] 1.1 Remove `@Transactional` from `user/adapter/outbound/UserRepositoryAdapter`
- [ ] 1.2 Remove `@Transactional` from `project/adapter/outbound/ProjectRepositoryAdapter`
- [ ] 1.3 Remove `@Transactional` from `project/adapter/outbound/UserIdentityLookupAdapter`
- [ ] 1.4 Remove `@Transactional` from `monthend/adapter/outbound/MonthEndTaskRepositoryAdapter`
- [ ] 1.5 Remove `@Transactional` from `monthend/adapter/outbound/MonthEndClarificationRepositoryAdapter`
- [ ] 1.6 Search for any remaining `@Transactional` usages in `..hexagon..adapter..` and remove them

## 2. Add ArchUnit enforcement rules

- [ ] 2.1 Add rule to `ArchitectureTest.java`: no class in `com.gepardec.mega.hexagon..` outside of `..application..` may be annotated with `@Transactional` (class or method level)
- [ ] 2.2 Add rule to `ArchitectureTest.java`: every class in `..hexagon..application..` that implements a `*UseCase` interface must be annotated with `@Transactional`

## 3. Verify

- [ ] 3.1 Run `mvn test -Dtest=ArchitectureTest` — both new rules must pass
- [ ] 3.2 Run `mvn test` — full test suite must pass with no regressions
