# Tasks

## 1. Quarkus upgrade and extension

- [ ] 1.1 Raise `quarkus.version` in `pom.xml` from 3.37.1 to 3.38.3. Review the Quarkus 3.38 migration guide for extensions we use and apply any required config or code changes. Verify with `mvn clean package`, where all tests pass with no other change.
- [ ] 1.2 Add `io.quarkiverse.httpproblem:quarkus-http-problem` without a `<version>`. Verify `mvn dependency:tree` shows version 3.38.2, managed by `io.quarkus.platform:quarkus-bom`, and that the application starts in `mvn quarkus:dev`.

## 2. Shared error model and rendering

- [ ] 2.1 Create `shared/domain/error/ErrorCategory` (`NOT_FOUND`, `FORBIDDEN`, `INVALID`) and the abstract `shared/domain/error/DomainException`, which takes a constant `code` and `category` through its constructors (with and without cause). Verify with a plain JUnit test that a subclass exposes both values.
- [ ] 2.2 Implement `shared/adapter/inbound/rest/DomainProblemMapper extends ExceptionMapperBase<DomainException>`. It maps the category to `404`/`403`/`400` and builds the problem from `HttpProblem.valueOf(status)` plus `detail` and `code`. Verify with a unit test for each category (status, title, detail, `code` parameter).
## 3. Hexagon exception migration

- [ ] 3.1 Make `MonthEndException`, `WorkTimeException` and `ProjectException` extend `DomainException`. Give each concrete subclass its constant code and category from the design's code catalogue. Verify that `mvn test` compiles and passes the existing domain and service tests.
- [ ] 3.2 Introduce a `UserException` base in `user/domain/error` and make `UnknownUsersException` extend it (`USER_UNKNOWN_USERS`, `INVALID`). Verify that the existing `UpdateInternalRatesService` tests pass.
- [ ] 3.3 Make the shared `ForbiddenException` extend `DomainException` (`FORBIDDEN`/`FORBIDDEN`) instead of `SecurityException`. Verify that `AuthenticatedActorContextTest` and the role-interceptor tests still pass.
- [ ] 3.4 Add `ProjectActorNotLeadException` (`PROJECT_ACTOR_NOT_LEAD`, `FORBIDDEN`) and throw it from `SetLeistungsnachweisEnabledService`. Update `SetLeistungsnachweisEnabledServiceTest` and `ProjectResourceTest` to expect the new type or code with status `403`.
- [ ] 3.5 Change `UserRepositoryAdapter.findByEmail` to throw `IllegalStateException` without the email in its message when several users share an email. Update `UserRepositoryAdapterDuplicateEmailTest` to assert the new type and that the message doesn't contain the email.
- [ ] 3.6 Delete `MonthEndDomainExceptionMapper`, `WorkTimeDomainExceptionMapper`, `ProjectDomainExceptionMapper` and the hexagon `shared/adapter/inbound/rest/ForbiddenExceptionMapper`. Verify with `mvn test` that the REST tests' status codes are unchanged (body assertions are migrated in 6.2).

## 4. Inbound adapter errors

- [ ] 4.1 Change `MonthEndRestTransportHelper` and `WorkTimeRestTransportHelper` to throw a `400` `HttpProblem` without `code` and with a `violations` entry (`field`, `in`, `message`). Add the parameter location to helper signatures where needed and update callers in `MonthEndResource`, `MonthEndCronResource`, `WorkTimeEmployeeResource` and `WorkTimeProjectLeadResource`. Verify with REST tests that an invalid month or payroll month returns the violation with `in: "path"` and no `code`.
- [ ] 4.2 Delete `MonthEndRestAdapterException`, `MonthEndRequestValidationException`, `WorkTimeRestAdapterException`, `WorkTimeRequestValidationException`, `MonthEndRestAdapterExceptionMapper` and `WorkTimeRestAdapterExceptionMapper`. Verify with `grep` that no references remain and that `mvn test` passes.
- [ ] 4.3 Change the CSV upload in `UserResource` to throw `400` `HttpProblem`s with codes `USER_INTERNAL_RATES_EMPTY_FILE`, `USER_INTERNAL_RATES_BAD_FORMAT` and `USER_INTERNAL_RATES_UNKNOWN_USERS`, plus a `lines` parameter, and to rethrow `IOException` unchecked. Verify with `UserResourceTest` assertions on `code`, `lines`, the absence of `errorCode`, and the `application/problem+json` media type.
- [ ] 4.4 Remove the `catch (Exception)` in `ZepMailWebhookResource`. Verify with a REST test that a failing use case yields a `500` problem without `code` and without the exception message.

## 5. Legacy cleanup

- [ ] 5.1 Delete the `application/exception/mapper` package (six mappers), `application/exception/UnauthorizedException`, `domain/model/ValidationViolation` and the six mapper unit tests in `src/test/java/.../application/exception/mapper`. Verify that `mvn test` passes and `grep` finds no remaining references.
- [ ] 5.2 Add a `@QuarkusTest` for one legacy endpoint (for example a `WorkerResource` operation made to fail through `@InjectMock`). Verify it returns a `500` problem without `code` in `application/problem+json`.

## 6. OpenAPI contract and tests

- [ ] 6.1 Replace `ApiError` in `openapi/schemas/shared.yaml` with `Problem` and `Violation` as specified in the design. Switch every response in `openapi/responses/common.yaml` to `application/problem+json`. Replace `InternalRateUploadError` with `InternalRateUploadProblem` (`allOf` `Problem` + `lines`) in `openapi/schemas/user.yaml` and `openapi/paths/user.yaml`, and keep `code` optional. Its description lists the known codes and states that framework-produced problems don't carry it. Verify that `mvn generate-sources` produces `ProblemDto`, `ViolationDto` and `InternalRateUploadProblemDto` and no longer produces `ApiErrorDto`.
- [ ] 6.2 Migrate `MonthEndResourceTest` and `WorkTimeEmployeeAndProjectLeadResourceTest` from `ApiErrorDto`/`message` to `ProblemDto` assertions on `status` and `code`. Verify that they pass.
- [ ] 6.3 Add a problem contract test in `src/test/java/.../hexagon/shared/adapter/inbound/rest`. It deserializes into `ProblemDto` and covers: a domain `404` (media type, `title`, `instance`, `code`, `detail`, no `type`), an adapter validation `400`, a bean-validation `400` (missing `enabled` on `PUT /projects/{projectId}/leistungsnachweis-enabled`), a `401` without `code`, a `403` role rejection with `code: FORBIDDEN`, a `500` without `detail` or `code`, and a success response still served as `application/json`. Verify that it passes.

## 7. Architecture rule and final check

- [ ] 7.1 Add an ArchUnit rule to `HexagonalArchitectureTest`: classes in `..hexagon..domain..` and `..hexagon..application..` must not depend on `jakarta.ws.rs..` or `io.quarkiverse.httpproblem..`. Verify with `mvn test -Dtest=HexagonalArchitectureTest`.
- [ ] 7.2 Run `mvn clean package`. Manually call one hexagon error endpoint and one legacy error endpoint in `mvn quarkus:dev` and confirm the extension logs 4xx at INFO and 5xx at ERROR, with no email address in the log lines.
