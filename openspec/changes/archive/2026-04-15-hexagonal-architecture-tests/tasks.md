## 1. Create HexagonalArchitectureTest class

- [x] 1.1 Create `src/test/java/com/gepardec/mega/hexagon/HexagonalArchitectureTest.java` with `@BeforeAll` setup importing only `com.gepardec.mega.hexagon` (exclude tests, jars, archives)
- [x] 1.2 Add ASCII diagram comment at the top of the class explaining the allowed dependency directions between domain, application, and adapter layers
- [x] 1.3 Define the `IMPLEMENTS_USE_CASE_INTERFACE` predicate (mirrors the one in `ArchitectureTest`)

## 2. Layer dependency rules

- [x] 2.1 Add `domainMustNotDependOnApplicationLayer` — `..hexagon..domain..` must not depend on `..hexagon..application..`
- [x] 2.2 Add `domainMustNotDependOnAdapterLayer` — `..hexagon..domain..` must not depend on `..hexagon..adapter..`
- [x] 2.3 Add `applicationMustNotDependOnAdapterLayer` — `..hexagon..application..` must not depend on `..hexagon..adapter..`
- [x] 2.4 Add `inboundAdaptersMustNotDependOnOutboundAdapters` — `..adapter..inbound..` must not depend on `..adapter..outbound..`
- [x] 2.5 Add `outboundAdaptersMustNotDependOnInboundAdapters` — `..adapter..outbound..` must not depend on `..adapter..inbound..`

## 3. Domain purity rules

- [x] 3.1 Add `domainModelsMustBeRecordsOrEnums` — non-enum types in `..hexagon..domain..model..` must be assignable to `Record.class`
- [x] 3.2 Add `domainModelsMustNotHaveJpaAnnotations` — types in `..hexagon..domain..model..` must not be annotated with any annotation from `jakarta.persistence..`

## 4. Port convention rules

- [x] 4.1 Add `inboundPortInterfacesMustEndWithUseCase` — interfaces in `..hexagon..application..port..inbound..` must have simple name ending with `UseCase`
- [x] 4.2 Add `inboundPortNonInterfacesMustBeRecords` — non-interface types in `..hexagon..application..port..inbound..` must be assignable to `Record.class`
- [x] 4.3 Add `outboundPortsMustBeInterfaces` — all types in `..hexagon..domain..port..outbound..` must be interfaces

## 5. Application service rules

- [x] 5.1 Add `applicationServicesMustEndWithService` — classes in `..hexagon..application..` implementing a `*UseCase` interface must have simple name ending with `Service`
- [x] 5.2 Add `applicationServicesMustBeApplicationScoped` — same classes must be annotated with `@ApplicationScoped`

## 6. Adapter placement rules

- [x] 6.1 Add `jpaEntitiesInHexagonMustResideInOutboundAdapter` — classes annotated with `@Entity` in `..hexagon..` must reside in `..hexagon..adapter..outbound..`
- [x] 6.2 Add `jpaEntitiesInHexagonMustEndWithEntity` — classes annotated with `@Entity` in `..hexagon..adapter..outbound..` must have simple name ending with `Entity`
- [x] 6.3 Add `panacheRepositoriesMustResideInOutboundAdapter` — classes assignable to `PanacheRepository` in `..hexagon..` must reside in `..hexagon..adapter..outbound..`

## 7. Move transaction rules from ArchitectureTest

- [x] 7.1 Add `hexagonClassesOutsideApplicationMustNotBeTransactional` — moved from `ArchitectureTest` (covers both class-level and method-level `@Transactional`)
- [x] 7.2 Add `hexagonUseCaseImplementationsMustBeTransactional` — moved from `ArchitectureTest`
- [x] 7.3 Delete `hexagonClassesOutsideApplicationShouldNotBeTransactional` from `ArchitectureTest.java`
- [x] 7.4 Delete `hexagonUseCaseImplementationsShouldBeTransactional` from `ArchitectureTest.java`

## 8. Verify

- [x] 8.1 Run `mvn test -Dtest=HexagonalArchitectureTest` and run `mvn test -Dtest=ArchitectureTest`
- [x] 8.2 For any failing rules, do NOT fix violations — instead write a summary of all violations grouped by rule and an action list of what would need to change to make them pass, then stop
