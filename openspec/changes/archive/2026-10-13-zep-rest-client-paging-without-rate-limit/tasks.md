# Tasks

## 1. Remove client-side rate limiting

- [x] 1.1 Remove `@RateLimit` and `@Retry` (and their imports) from `ZepAttendanceRestClient`, `ZepEmployeeRestClient`, `ZepProjectRestClient` and `ZepReceiptRestClient`; verify with `grep -rnE "RateLimit|faulttolerance" src/main/java` returning no matches
- [x] 1.2 Remove the `quarkus-smallrye-fault-tolerance` dependency from `pom.xml` and the `quarkus.fault-tolerance` block from `application.yaml`; verify `mvn compile` succeeds and dev-mode startup logs no unrecognized `quarkus.fault-tolerance` config warning

## 2. Request the maximum page size

- [x] 2.1 Add `mega.zep.page-size: 200` under `mega.zep` in `application.yaml`; verify the value is resolvable by starting `mvn quarkus:dev` without config errors
- [x] 2.2 Add class-level `@ClientQueryParam(name = "limit", value = "${mega.zep.page-size}")` to the four ZEP REST client interfaces; verify no client method signature changed (`git diff` shows annotation/import changes only) and `mvn test -Dtest='com.gepardec.mega.zep.rest.service.*Test,AbsenceServiceTest'` passes
- [x] 2.3 Smoke-check against the ZEP test instance in dev mode with REST client request logging enabled (`quarkus.rest-client.logging.scope=request-response`, `quarkus.log.category."org.jboss.resteasy.reactive.client.logging".level=DEBUG`): load a worktime view and verify the logged attendance request carries `limit=200` and that a single-resource call (e.g. employee by username) still returns 200

## 3. Unknown-field tolerance

- [x] 3.1 Verify every response type in `com.gepardec.mega.zep.rest.dto` is annotated with `@JsonIgnoreProperties(ignoreUnknown = true)`: `grep -L "JsonIgnoreProperties" src/main/java/com/gepardec/mega/zep/rest/dto/*.java` reports no file
- [x] 3.2 Remove the ineffective `@JsonIgnoreProperties` (and its import) from the `ZepAttendanceRestClient`, `ZepEmployeeRestClient` and `ZepProjectRestClient` interfaces; verify `grep -n "JsonIgnoreProperties" src/main/java/com/gepardec/mega/zep/rest/client/*.java` returns no matches and `mvn test -Dtest='com.gepardec.mega.zep.rest.service.*Test,AbsenceServiceTest'` passes

## 4. Cleanup and integration

- [x] 4.1 Delete `scripts/zep-rate-limit-probe.sh` (and the empty `scripts/` directory); verify its content is preserved in the appendix of `design.md`
- [x] 4.2 Run the full build `mvn clean package` and verify all tests, including `ArchitectureTest`, pass
