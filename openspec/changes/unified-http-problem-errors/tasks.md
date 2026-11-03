# Tasks

## 1. Quarkus upgrade and extension

- [x] 1.1 Make sure the Quarkus platform BOM manages `quarkus-http-problem` (3.38 or newer). Originally done by raising `quarkus.version` from 3.37.1 to 3.38.3; after rebasing onto `main` the project is on 3.40.1, which manages extension version 3.38.2. Verify with `mvn clean package`.
- [x] 1.2 Add `io.quarkiverse.httpproblem:quarkus-http-problem` without a `<version>`. Verify `mvn dependency:tree` shows version 3.38.2, managed by `io.quarkus.platform:quarkus-bom`, and that the application starts in `mvn quarkus:dev`.
- [x] 1.3 Delete the legacy `JacksonObjectMapperContextResolver` and set `quarkus.jackson.fail-on-unknown-properties: true` (D11). Verify with `mvn test` that problem bodies no longer contain `stackTrace` or `cause` and carry `code` at the top level.

## 2. Shared error model and rendering

- [x] 2.1 Create `shared/domain/error/ErrorCategory` (`NOT_FOUND`, `FORBIDDEN`, `INVALID`) and the abstract `shared/domain/error/DomainException`, which takes a constant `code` and `category` through its constructors (with and without cause). Verify with a plain JUnit test that a subclass exposes both values.
- [x] 2.2 Implement `shared/adapter/inbound/rest/DomainProblemMapper extends ExceptionMapperBase<DomainException>`. It maps the category to `404`/`403`/`400` and builds the problem from `HttpProblem.valueOf(status)` plus `detail` and `code`. Verify with a unit test for each category (status, title, detail, `code` parameter).
## 3. Hexagon exception migration

- [x] 3.1 Make `MonthEndException`, `WorkTimeException` and `ProjectException` extend `DomainException`. Give each concrete subclass its constant code and category from the design's code catalogue. Verify that `mvn test` compiles and passes the existing domain and service tests.
- [x] 3.2 Introduce a `UserException` base in `user/domain/error` and make `UnknownUsersException` extend it (`USER_UNKNOWN_USERS`, `INVALID`). Verify that the existing `UpdateInternalRatesService` tests pass.
- [x] 3.3 Make the shared `ForbiddenException` extend `DomainException` (`FORBIDDEN`/`FORBIDDEN`) instead of `SecurityException`. Verify that `AuthenticatedActorContextTest` and the role-interceptor tests still pass.
- [x] 3.4 Add `ProjectActorNotLeadException` (`PROJECT_ACTOR_NOT_LEAD`, `FORBIDDEN`) and throw it from `SetLeistungsnachweisEnabledService`. Update `SetLeistungsnachweisEnabledServiceTest` and `ProjectResourceTest` to expect the new type or code with status `403`.
- [x] 3.5 Change `UserRepositoryAdapter.findByEmail` to throw `IllegalStateException` without the email in its message when several users share an email. Update `UserRepositoryAdapterDuplicateEmailTest` to assert the new type and that the message doesn't contain the email.
- [x] 3.6 Delete `MonthEndDomainExceptionMapper`, `WorkTimeDomainExceptionMapper`, `ProjectDomainExceptionMapper` and the hexagon `shared/adapter/inbound/rest/ForbiddenExceptionMapper`. Verify with `mvn test` that the REST tests' status codes are unchanged (body assertions are migrated in 6.2).
- [x] 3.7 Introduce the shared `ErrorCode` interface (code derived as `<boundedContext>_<name>`), `SharedErrorCode` and one error-code enum per bounded context, and make `DomainException` and the context base exceptions take them. Add `ErrorCodeTest`, which checks that codes are unique and well-formed and pins the full list. Verify that `mvn test` passes with every wire code unchanged.
- [x] 3.8 Make `MonthEndException`, `ProjectException`, `WorkTimeException` and `UserException` concrete, taking their context's error-code enum, and delete the per-failure subclasses except `UnknownUsersException`. Move throw sites to the context exception with the matching constant, and move tests from class assertions to `errorCode()` assertions. Verify that `mvn test` passes with every wire code unchanged.

## 4. Inbound adapter errors

- [x] 4.1 Remove adapter-side input parsing. Type month fields and parameters as `format: year-month`, map the format to `YearMonth` in the `jaxrs-spec` generator config, make `YearMonthParamConverter` throw `BadRequestException` for malformed values, and delete `MonthEndRestTransportHelper`, `WorkTimeRestTransportHelper` and `RequestValidationProblems`. Resources take `YearMonth` and wrap identifiers inline. Verify with REST tests that an invalid month or payroll month returns a `400` problem without `code` or `violations`.
- [x] 4.2 Delete `MonthEndRestAdapterException`, `MonthEndRequestValidationException`, `WorkTimeRestAdapterException`, `WorkTimeRequestValidationException`, `MonthEndRestAdapterExceptionMapper` and `WorkTimeRestAdapterExceptionMapper`. Verify with `grep` that no references remain and that `mvn test` passes.
- [x] 4.3 Change the CSV upload in `UserResource` to throw `400` `HttpProblem`s with codes `USER_INTERNAL_RATES_EMPTY_FILE`, `USER_INTERNAL_RATES_BAD_FORMAT` and `USER_INTERNAL_RATES_UNKNOWN_USERS`, plus a `lines` parameter, and to rethrow `IOException` unchecked. Verify with `UserResourceTest` assertions on `code`, `lines`, the absence of `errorCode`, and the `application/problem+json` media type.
- [x] 4.4 Remove the `catch (Exception)` in `ZepMailWebhookResource`. Verify with a REST test that a failing use case yields a `500` problem without `code` and without the exception message.
- [x] 4.5 Rename the body parameters of `MonthEndResource` to the names in the generated `MonthEndApi` (D12). Verify with a REST test that a bean-validation violation on a month-end request body reports `in: "body"` and a `field` without the parameter-name prefix.

## 5. Legacy cleanup

- [x] 5.1 Delete the `application/exception/mapper` package (six mappers), `application/exception/UnauthorizedException`, `domain/model/ValidationViolation` and the six mapper unit tests in `src/test/java/.../application/exception/mapper`. Verify that `mvn test` passes and `grep` finds no remaining references.
- [x] 5.2 Add a `@QuarkusTest` for one legacy endpoint (for example a `WorkerResource` operation made to fail through `@InjectMock`). Verify it returns a `500` problem without `code` in `application/problem+json`.

## 6. OpenAPI contract and tests

- [x] 6.1 Replace `ApiError` in `openapi/schemas/shared.yaml` with `Problem` and `Violation` as specified in the design. Switch every response in `openapi/responses/common.yaml` to `application/problem+json`. Replace `InternalRateUploadError` with `InternalRateUploadProblem` (`allOf` `Problem` + `lines`) in `openapi/schemas/user.yaml` and `openapi/paths/user.yaml`, and keep `code` optional. Its description states the naming rule and that framework-produced problems don't carry it, without listing individual codes. Verify that `mvn generate-sources` produces `ProblemDto`, `ViolationDto` and `InternalRateUploadProblemDto` and no longer produces `ApiErrorDto`.
- [x] 6.2 Migrate `MonthEndResourceTest` and `WorkTimeResourceTest` (formerly `WorkTimeEmployeeAndProjectLeadResourceTest`) from `ApiErrorDto`/`message` to `ProblemDto` assertions on `status` and `code`. Verify that they pass.
- [x] 6.3 Add a problem contract test in `src/test/java/.../hexagon/shared/adapter/inbound/rest`. It deserializes into `ProblemDto` and covers: a domain `404` (media type, `title`, `instance`, `code`, `detail`, no `type`), a malformed path value `400` without `code` or `violations`, a bean-validation `400` (missing `enabled` on `PUT /projects/{projectId}/leistungsnachweis-enabled`), a `401` without `code`, a `403` role rejection with `code: FORBIDDEN`, a `500` without `detail` or `code`, and a success response still served as `application/json`. Verify that it passes.
- [x] 6.4 Set the `jaxrs-spec` option `generateJsonCreator` to `false` and replace required-args DTO constructor calls with the fluent setters. Add an optional `field` member to the `Problem` schema (D13). Verify that `PUT /projects/{projectId}/leistungsnachweis-enabled` with `{}` returns `violations` with `field: "enabled"` and `in: "body"`, and that a non-boolean `enabled` returns a `400` problem without `code` or `violations`.

## 7. Architecture rule and final check

- [x] 7.1 Add an ArchUnit rule to `HexagonalArchitectureTest`: classes in `..hexagon..domain..` and `..hexagon..application..` must not depend on `jakarta.ws.rs..` or `io.quarkiverse.httpproblem..`. Verify with `mvn test -Dtest=HexagonalArchitectureTest`.
- [x] 7.2 Run `mvn clean package`. Manually call one hexagon error endpoint and one legacy error endpoint in `mvn quarkus:dev` and confirm the extension logs 4xx at INFO and 5xx at ERROR, with no email address in the log lines.

## 8. Drift after rebasing onto the capacity-unified endpoints

- [x] 8.1 Add `shared/adapter/inbound/rest/OutboundHttpFailureProblemMapper extends ExceptionMapperBase<ClientWebApplicationException>` that answers `502` without `detail` or `code` (D14). Verify with a unit test for the mapper and a case in the problem contract test where a use case throws a `ClientWebApplicationException` with status `404` and the API answers `502` without `detail` or `code`.
- [x] 8.2 Rename the body parameter of `WorkTimeWarningRuleResource.updateWarningRuleSetting` to the generated name and add a unit test that checks every resource implementing a generated API interface uses the interface's parameter names (D15). Verify with a REST test that `PUT /worktime/warning-rules/{rule}` with `{}` reports `violations` with `field: "enabled"` and `in: "body"`.
- [x] 8.3 Run `mvn clean package` and the OpenRewrite hygiene check (`rewrite-check` profile) on the rebased branch. Verify both pass.

## 9. Structural invariants as server errors

- [x] 9.1 Make the compact constructors of `MonthEndTask` and `MonthEndClarification` throw `IllegalArgumentException`, and make the missing-ZEP-username checks in the work-time services throw `IllegalStateException` (D16). Remove `MonthEndErrorCode.VALIDATION_FAILED` and `WorkTimeErrorCode.VALIDATION_FAILED`. Verify with domain tests that a stored clarification breaking an invariant fails with `IllegalArgumentException`.
- [x] 9.2 Add `MonthEndErrorCode.TASK_TYPE_NOT_BULK_COMPLETABLE` for the bulk-completion type rule and `MonthEndErrorCode.NO_ELIGIBLE_PROJECT_LEAD`, checked by the clarification creation factory. Pin both in `ErrorCodeTest`. Verify with a domain test that creating a clarification without eligible leads yields `NO_ELIGIBLE_PROJECT_LEAD`.
- [x] 9.3 Add `@NotBlank` to `text` in `CreateClarificationRequest` and `UpdateClarificationTextRequest`. Verify with REST tests that a blank text returns `400` with a `violations` entry `field: "text"`, `in: "body"`, and no `code`.
- [x] 9.4 Run `mvn clean package -Prewrite-check`. Verify it passes.

## 10. Review follow-ups: one source per code, lenient REST clients

- [x] 10.1 Rename `UserErrorCode.UNKNOWN_USERS` to `INTERNAL_RATES_UNKNOWN_USERS` and let `UserResource` reuse the exception's code. Move the empty-file and bad-format codes from string constants into the adapter-local enum `InternalRateUploadErrorCode`. Pin all three in `ErrorCodeTest`. Verify that `UserResourceTest` still sees the unchanged wire codes.
- [x] 10.2 Replace `quarkus.jackson.fail-on-unknown-properties: true` with `StrictServerObjectMapperResolver` constrained to the server (D11). Verify with a `@QuarkusTest` that a REST client ignores an unknown response property and that the API still rejects an unknown request property with `400`.
- [x] 10.3 Run `mvn clean package -Prewrite-check`. Verify it passes.

## 11. Status by kind of rejection

- [x] 11.1 Add the categories `CONFLICT` and `UNPROCESSABLE` and map them to `409` and `422` in `DomainProblemMapper`, exposing the mapping as `problemOf(ErrorCode)` (D17). Recategorise the codes as in the catalogue and let `UserResource` build its CSV problems from `problemOf`. Verify with `DomainProblemMapperTest` that each category yields its status and title.
- [x] 11.2 Pin each code together with its category in `ErrorCodeTest`. Verify that it passes.
- [x] 11.3 Add `Conflict` and `UnprocessableContent` to `openapi/responses/common.yaml` and reference them from the operations that can return them; give the CSV upload a `422` response with `InternalRateUploadProblem`. Verify with REST tests that a closed clarification yields `409`, an unassigned subject employee and an unsupported bulk type yield `422`, enabling Leistungsnachweis on a non-billable project yields `409`, and unknown CSV users yield `422` with `lines`.
- [x] 11.4 Run `mvn clean package -Prewrite-check`. Verify it passes.
