# Design

## Context

See proposal.md for the motivation. How errors are produced today:

```
 hexagon                                                  legacy (global, all endpoints)
 -------                                                  ------------------------------
 MonthEndException (7) --> MonthEndDomainExceptionMapper   ApplicationExceptionMapper (Exception) -> 500, empty
 WorkTimeException (2) --> WorkTimeDomainExceptionMapper   WebApplicationExceptionMapper          -> passthrough
 ProjectException (2)  --> ProjectDomainExceptionMapper    ConstraintViolationExceptionMapper     -> [{property,message}]
 *RestAdapterException --> 2 adapter mappers               HibernateConstraintViolation...Mapper  -> 400 (dead: table dropped)
 shared ForbiddenException --> ForbiddenExceptionMapper    UnauthorizedExceptionMapper            -> 401, empty (exception never thrown)
 UnknownUsersException --> caught in UserResource          ZepExceptionMapper                     -> 500, empty
       all of the above --> ApiError{message}
```

Constraints:
- The API is contract-first. `src/main/resources/openapi/*.yaml` is bundled into `target/generated-sources/openapi-bundle/openapi/openapi.yaml`, which is the input to `jaxrs-spec` **and** the source the frontend generates its client from. Anything the extension adds to the served `/q/openapi` never reaches the frontend.
- The bundle only contains hexagon paths. Legacy endpoints get problem responses at runtime but have no contract.
- `quarkus.http.auth.proactive` is `false`, so authentication failures pass through JAX-RS exception mappers.
- The hexagon `ForbiddenException` (`shared/application/security`) is a hexagon-owned `SecurityException`, not the JAX-RS type. Its throw sites don't leak HTTP today. They're touched here only so they get a category and code.
- `quarkus-http-problem` versions follow Quarkus versions. `io.quarkus.platform:quarkus-bom` manages it from 3.38.0; 3.37.1 (our current version) doesn't. Platform BOMs 3.38.3 and 3.39.4 both manage extension version `3.38.2`.

## Goals / Non-Goals

**Goals:**
- One rendering path for all problems: the extension. Our code only classifies errors.
- Hexagon code classifies each failure once, at its definition, with no HTTP knowledge.
- The OpenAPI `Problem` schema matches the actual JSON on the wire, and a test enforces it.

**Non-Goals:**
- Success-side messages or response envelopes.
- Re-evaluating the HTTP status of existing domain failures, for example whether a closed clarification should be `409`. All current statuses are kept (spec: *Business-rule rejections keep their HTTP status*).
- Documenting legacy endpoints in the OpenAPI contract.
- Frontend changes, including the planned error interceptor.
- Adding `type` URIs or a problem-type registry.

## Decisions

### D1: `quarkus-http-problem`, version managed by the platform BOM (Quarkus 3.38.3)
We add `io.quarkiverse.httpproblem:quarkus-http-problem` without a `<version>` and raise `quarkus.version` from 3.37.1 to **3.38.3**, the latest patch of the first platform release that manages the extension. We choose 3.38.3 over 3.39.4 because it's the smallest upgrade that achieves the goal; 3.39.x brings no extension difference (same `3.38.2`).
- *Alternatives:* pin `3.33.2` on 3.37.1, which the user rejected because they want the BOM to manage it. Or write our own mappers, which would re-implement mappers for validation, auth, Jackson and catch-all, plus logging.

### D2: Shared, HTTP-free `DomainException` with `code` and `ErrorCategory`
`shared/domain/error/DomainException` (abstract, extends `RuntimeException`) exposes `String code()` and `ErrorCategory category()`. `ErrorCategory` is an enum `NOT_FOUND | FORBIDDEN | INVALID`, and we add values only when a failure needs them. Each BC's existing abstract base (`MonthEndException`, `WorkTimeException`, `ProjectException`) extends `DomainException`. Every concrete exception passes a **constant** code and category to `super(...)`, so both are fixed per class, not per throw site. `UnknownUsersException` gets a `UserException` base like the other contexts.

The shared `ForbiddenException` (still in `shared/application/security`) also extends `DomainException`, with code `FORBIDDEN` and category `FORBIDDEN`. It currently extends `SecurityException`; nothing catches `SecurityException`, so changing the superclass is safe.

- *Why a category and not an HTTP status on the exception:* the domain states the *kind* of failure. Only the inbound adapter knows it becomes a `404`. This keeps the new ArchUnit rule (spec: hexagon-layer-constraints) satisfiable.
- *Why in the shared kernel:* the category is a stable concept shared across contexts, which is exactly what the shared-kernel requirement allows.
- *Alternative:* keep per-BC `instanceof` mapping. That's rejected because it's the duplication we're removing.

**Per-context error-code enums.** Each bounded context declares its codes in one enum in its `domain/error` package (`MonthEndErrorCode`, `ProjectErrorCode`, `WorkTimeErrorCode`, `UserErrorCode`), and each constant carries its `ErrorCategory`. The enums implement the shared `ErrorCode` interface, whose default `code()` derives the wire code as `<boundedContext>_<constant name>`, so the context prefix holds by construction. The shared `FORBIDDEN` lives in `SharedErrorCode`, which overrides `code()` to drop the prefix. `DomainException` takes an `ErrorCode`, and each context's base exception accepts only that context's enum. `ErrorCodeTest` finds every `ErrorCode` enum in the hexagon, checks that codes are unique and well-formed, and pins the full list of codes. Because a renamed constant changes the wire code, the pinned list makes such a rename fail the build until it's deliberately updated, and it gives an overview of all codes in one place.
- *Alternatives:* one global enum or constants class, rejected because every context would then depend on a shared list of all failures; explicit code strings per constant, rejected because they duplicate the constant name and leave the prefix to convention.

**One exception type per context.** Because the enum constant already names the failure and carries its category, each context has one concrete exception (`MonthEndException`, `ProjectException`, `WorkTimeException`, `UserException`) that takes its own enum constant, for example `new MonthEndException(MonthEndErrorCode.TASK_NOT_FOUND, message)`. A dedicated subclass exists only when the failure carries structured data or code must catch exactly that failure. Today that's `UnknownUsersException`, which `UserResource` catches to turn usernames into CSV line numbers. The shared `ForbiddenException` stays as it is. Tests assert on `errorCode()` instead of the exception class.
- *Alternative:* one subclass per failure. Rejected because the subclasses repeated the enum constants without adding behaviour, and nothing caught them by type.

**Code catalogue.** These are the stable values; the frontend switches on them. The status column must match today's behaviour.

| Exception | Category | Status | Code |
|---|---|---|---|
| `MonthEndTaskNotFoundException` | NOT_FOUND | 404 | `MONTHEND_TASK_NOT_FOUND` |
| `MonthEndClarificationNotFoundException` | NOT_FOUND | 404 | `MONTHEND_CLARIFICATION_NOT_FOUND` |
| `MonthEndActorNotAuthorizedException` | FORBIDDEN | 403 | `MONTHEND_ACTOR_NOT_AUTHORIZED` |
| `MonthEndClarificationClosedException` | INVALID | 400 | `MONTHEND_CLARIFICATION_CLOSED` |
| `MonthEndEmployeeContextNotFoundException` | INVALID | 400 | `MONTHEND_EMPLOYEE_CONTEXT_NOT_FOUND` |
| `MonthEndEmployeeNotAssignedToProjectException` | INVALID | 400 | `MONTHEND_EMPLOYEE_NOT_ASSIGNED_TO_PROJECT` |
| `MonthEndProjectContextNotFoundException` | INVALID | 400 | `MONTHEND_PROJECT_CONTEXT_NOT_FOUND` |
| `MonthEndValidationException` | INVALID | 400 | `MONTHEND_VALIDATION_FAILED` |
| `ProjectNotFoundException` | NOT_FOUND | 404 | `PROJECT_NOT_FOUND` |
| `LeistungsnachweisNotApplicableException` | INVALID | 400 | `PROJECT_LEISTUNGSNACHWEIS_NOT_APPLICABLE` |
| `ProjectActorNotLeadException` (new) | FORBIDDEN | 403 | `PROJECT_ACTOR_NOT_LEAD` |
| `WorkTimeUserNotFoundException` | NOT_FOUND | 404 | `WORKTIME_USER_NOT_FOUND` |
| `WorkTimeValidationException` | INVALID | 400 | `WORKTIME_VALIDATION_FAILED` |
| `UnknownUsersException` | INVALID | 400 | `USER_UNKNOWN_USERS` |
| shared `ForbiddenException` | FORBIDDEN | 403 | `FORBIDDEN` |

Adapter-level codes (D5): `USER_INTERNAL_RATES_EMPTY_FILE`, `USER_INTERNAL_RATES_BAD_FORMAT`, `USER_INTERNAL_RATES_UNKNOWN_USERS`. Problems produced by the framework have no code (D4).

### D3: One `DomainProblemMapper` extending the extension's `ExceptionMapperBase`
`shared/adapter/inbound/rest/DomainProblemMapper extends ExceptionMapperBase<DomainException>`, with constructor injection of `PostProcessorsRegistry`. `toProblem` builds `HttpProblem.valueOf(status)`, which sets `title` to the reason phrase, then adds `detail = exception.getMessage()` and `.with("code", code)`. The category-to-status mapping lives only here: `NOT_FOUND → 404`, `FORBIDDEN → 403`, `INVALID → 400`. JAX-RS picks the most specific mapper, so it wins over the extension's `DefaultExceptionMapper` (`Exception`). Because it extends the base class, post-processing (logging, MDC) runs for domain problems exactly as for built-in ones.
- *Alternatives (discussed and rejected):* a post-processor that rewrites the default `500`, which only uses documented API but is indirect; a CDI interceptor that translates to `HttpProblem`, which needs a binding on every resource and misses non-resource paths; or a plain `ExceptionMapper` with `HttpProblem.toResponse()`, which skips post-processing and logging.
- *Trade-off:* `ExceptionMapperBase` is documented only in `CONTRIBUTING.md`, so it's effectively internal API. See the risks section.

### D4: `code` only on problems we raise
Only `DomainProblemMapper` (D3) and adapter-thrown problems for endpoint-specific checks (D5, the CSV upload) set `code`. Problems from the extension's built-in mappers (auth, bean validation, not found, JSON errors, catch-all) and framework rejections of unparsable path or query values carry no `code`. `code` therefore means exactly one thing: there is a specific reason beyond the HTTP status. The frontend's rule is `code ?? status`, plus the presence of `violations` for inline field errors. It needs that rule anyway for errors that never come from our application: ingress and proxy 5xx responses, Keycloak, and network failures.
- *Alternative (rejected):* a `ProblemPostProcessor` that derives a fallback code from the status (`UNAUTHORIZED`, `INTERNAL_SERVER_ERROR`, …) or `VALIDATION_FAILED`. Such a code is fully predictable from `status` and `violations`, so it adds no information. It would also make `code` look guaranteed in the contract when it can't be guaranteed end to end, and it's one more extension hook to maintain.

### D5: Adapters don't parse input by hand
Months are declared `type: string, format: year-month` in the contract, and `jaxrs-spec` maps that format to `java.time.YearMonth` through `typeMappings` `string+year-month=YearMonth` and an identity mapping `YearMonth=YearMonth`. The identity mapping is needed because generator 7.12 otherwise applies `modelNameSuffix` and generates `YearMonthDto`. Path and query months go through the existing `YearMonthParamConverter`, which throws `BadRequestException` for a malformed value, so the extension's `WebApplicationExceptionMapper` answers `400` without `code` or `violations`. The converter must throw a `WebApplicationException`, because RESTEasy Reactive turns any other converter exception on a path or query parameter into `404`. Months in request bodies are read by Jackson, and malformed ones reach the extension's Jackson mappers (D13).

Identifiers are already `UUID` in the contract, and resources wrap them inline (`ProjectId.of(uuid)`). Path parameters can't be null, and required body fields are covered by bean validation, so no adapter null check is needed. A malformed UUID path parameter gets RESTEasy Reactive's default `404`. `MonthEndRestTransportHelper`, `WorkTimeRestTransportHelper`, `RequestValidationProblems`, `MonthEndRestAdapterException`, `MonthEndRequestValidationException`, `WorkTimeRestAdapterException`, `WorkTimeRequestValidationException` and both RestAdapter mappers are removed.
- *Alternative (rejected):* Keep adapter-side parsing so malformed path and query values produce `violations` naming the parameter. That needs either parsing code in every resource, or a param converter plus a custom mapper that recovers the parameter name from the request, because RESTEasy Reactive passes no parameter annotations to `ParamConverterProvider`. We prefer the framework default, because a client can't fix a malformed path value at runtime anyway.
- *Alternative (rejected):* Map identifiers to domain value objects through generator type mappings. That puts domain types into the generated HTTP contract classes and needs a Jackson creator and a param converter for each ID type.

`UserResource` (CSV upload) throws `HttpProblem` with `400`, the `USER_INTERNAL_RATES_*` code and `.with("lines", lines)`, and still catches `UnknownUsersException` to translate usernames into line numbers. `IOException` while reading the file is rethrown as unchecked, so the catch-all turns it into a `500`.

`ZepMailWebhookResource` no longer catches `Exception`. The catch-all produces the `500` problem and logs at ERROR with the stack trace, which replaces the explicit `Log.error`. Pub/Sub only looks at the status, so retry behaviour is unchanged.

### D6: Non-authorization faults become plain unchecked exceptions
`UserRepositoryAdapter.findByEmail` throws `IllegalStateException("authenticated actor resolution is ambiguous: N users share one email")`, without the email, so it becomes a `500`. `SetLeistungsnachweisEnabledService` throws the new `ProjectActorNotLeadException` instead of the shared `ForbiddenException`, the same pattern as `MonthEndActorNotAuthorizedException`. The status stays `403`; the code becomes more specific.

### D7: The contract owns `Problem`; the extension's OpenAPI additions are ignored
In `openapi/schemas/shared.yaml`, `ApiError` is replaced by:
- `Problem`: `title` (string), `status` (int32), `instance` (string), `detail` (string, optional), `code` (string, optional; its description states the naming rule and that framework-produced problems don't carry it, but doesn't list individual codes, because such a list goes stale as codes are added), `violations` (array of `Violation`, optional), `field` (string, optional; set by the extension on malformed-body problems, D13). `additionalProperties` is left unset: OpenAPI then still allows extra members such as `lines`, while `additionalProperties: true` would make `jaxrs-spec` generate `ProblemDto extends HashMap`, which Jackson deserializes as a plain map with empty getters. Required fields are `title` and `status`.
- `Violation`: `field` (string), `in` (enum `path|query|header|form|body|?`), `message` (string), all required. `?` is the extension's token for a location it can't determine (D12).

In `openapi/schemas/user.yaml`, `InternalRateUploadError` becomes `InternalRateUploadProblem` = `allOf[Problem, {lines: int[]}]`. Every entry in `responses/common.yaml` and the CSV `400` switches to `application/problem+json`. As verified with generator 7.12.0, `jaxrs-spec` then generates `@Produces({"application/json", "application/problem+json"})`, which is harmless because success responses are still negotiated as JSON. Operations without a JSON success body (`POST /monthend/generate-prematurely`, `DELETE /monthend/clarifications/{clarificationId}`, `POST /users/internal-rates`) now produce only `application/problem+json`, so a client that sends a strict `Accept: application/json` to them gets `406`. We accept this as part of the breaking change: clients generated from the contract send the matching `Accept`, and browser defaults include `*/*`. The generated `ProblemDto` and `ViolationDto` exist only for the frontend and tests; the backend never builds them. The extension's own `HttpProblem` and `HttpValidationProblem` schemas in `/q/openapi` stay as served-doc noise and are not configured.
- *Alternative:* let the extension own the schema (error responses without content). Rejected because the frontend generates from the bundle, which would then contain no error schema.

### D8: Remove all legacy mappers
The whole `application/exception/mapper` package, `application/exception/UnauthorizedException` (never thrown), `domain/model/ValidationViolation` (used only by the removed mapper) and their six unit tests are removed. The hexagon `ForbiddenExceptionMapper` is removed too. The extension's logger replaces its WARN line, which also stops logging the user's email.

### D9: Logging
We rely on the extension: 4xx logged at INFO, 5xx at ERROR with the stack trace, under log category `http-problem`. No MDC properties for now. `quarkus.http-problem.include-details` stays `false`, so built-in mappers don't copy exception messages into `detail`. Only `DomainProblemMapper` sets `detail`, from domain messages we control.

### D11: The Quarkus-managed `ObjectMapper` serializes all responses
The legacy `JacksonObjectMapperContextResolver` returns its own `new ObjectMapper()`, which skips every `ObjectMapperCustomizer`, including the one that registers the extension's `HttpProblem` serializer. With it in place, problems are rendered as plain bean dumps that contain `stackTrace`, `cause` and a nested `parameters` object. We delete the resolver and rely on the Quarkus-managed `ObjectMapper`, which already registers `JavaTimeModule` and writes dates as ISO strings. We set `quarkus.jackson.fail-on-unknown-properties: true` to keep the resolver's strict deserialization (Jackson's default, which Quarkus turns off).
- *Alternative:* register the extension's module inside the resolver by hand. Rejected because it depends on extension internals and the resolver would keep ignoring every other Quarkus customizer.

### D12: Bean-validation violation locations
The extension resolves a body violation to `in: "body"` only when the resource method's parameter name matches the generated interface's. Otherwise it reports `in: "?"` and prefixes `field` with the parameter name. We rename the `MonthEndResource` body parameters to the generated names, so body violations resolve to `body`. The contract lists `?` as a valid `in` value, because that's the extension's actual token for an unknown location.
- *Alternatives:* a `ProblemPostProcessor` that rewrites `?` to `unknown`, which is one more hook to maintain; or dropping the unknown value from the contract, which the extension can't guarantee.

### D13: Missing body fields go through bean validation; malformed bodies keep the extension's shape
With `jaxrs-spec`'s default `generateJsonCreator=true`, request DTOs get a `@JsonCreator` with `@JsonProperty(required = true)`. A missing required field then fails inside Jackson before bean validation runs, and the extension's `MismatchedInputExceptionMapper` answers with `detail: "Malformed request body"` and a top-level `field` (often `?`) in place of `violations`. We set `generateJsonCreator=false`: a missing field deserializes to `null`, and the generated `@NotNull` reports it as a `violations` entry with `in: "body"`. Malformed JSON and values of the wrong JSON type still reach the extension's Jackson mappers. We accept their shape (`400`, no `code`, no `violations`, optional `field`), and the `Problem` schema declares `field` as optional.
- *Alternatives:* a `ProblemPostProcessor` that turns the Jackson `field` problems into `violations`, which adds a hook for a case clients can't fix at runtime anyway; or keeping the creators and post-processing everything, where missing fields would only get `field: "?"`.

### D10: Tests
- A REST-Assured contract test class in `shared/adapter/inbound/rest` covering:
  - a domain `404` (status, media type, `title`, `instance`, `code`, `detail`, no `type`);
  - a malformed path value `400` without `code` or `violations`;
  - a bean-validation `400`;
  - a `401` without `code`, and a `403` (role check) with `code: FORBIDDEN`;
  - a `500` without `detail` or `code`;
  - success responses still using `application/json`.
- Deserialize problems into the generated `ProblemDto`, so drift between schema and wire fails the test. REST-Assured's mapper rejects unknown members, so an unexpected extra member on a generic problem fails the test too.
- Existing REST tests that read `ApiErrorDto` (`MonthEndResourceTest`, `WorkTimeEmployeeAndProjectLeadResourceTest`) and `errorCode` (`UserResourceTest`) assert on `code` instead.
- `HexagonalArchitectureTest` gets the JAX-RS / problem-library rule for `..domain..` and `..application..`.

## Risks / Trade-offs

- [`ExceptionMapperBase` is internal API and could change in an extension release] → The contract test in D10 covers domain problems end to end, so a breaking upgrade fails the build. If that happens, switch to the post-processor approach (D3 alternative), which needs no contract change.
- [The Quarkus 3.37 → 3.38 upgrade brings unrelated changes] → Do it as the first, separate step. Read the 3.38 migration guide and run the full test suite before adding the extension.
- [Extension mappers take priority over custom ones for the same exception type] → We don't register any mapper for a built-in type. `DomainProblemMapper` targets our own type only.
- [Frontend breaks on every error-handling path at once] → This is accepted and the change is marked breaking. Deploy together with the frontend release that reads `code`. There's no transition mode.
- [Legacy endpoints change error bodies without a contract] → They were empty or ad hoc before. The frontend's future interceptor reads `code` from any `application/problem+json` response.
- [Domain messages end up in `detail` and might contain identifiers] → Current messages hold IDs and field names, not personal data. The `UserRepositoryAdapter` message that contained an email becomes a 500, which never gets `detail`. New domain messages must not include personal data.
- [`WorkTimeValidationException` ("zep username missing for user") is a data problem reported as `400`] → Kept to preserve behaviour (non-goal). It's a follow-up candidate for reclassification as a server error.
- [A `WebApplicationException` carrying a `Response` entity bypasses every mapper (per the extension's TROUBLESHOOTING.md)] → No such throw sites exist. The only `WebApplicationException` user is `PersonioEmployeeAdapter`, which catches REST-client exceptions and doesn't rethrow them to the inbound side.

## Migration Plan

1. Upgrade Quarkus alone; the suite must stay green.
2. Add the extension, the shared error types and `DomainProblemMapper`, then remove the old mappers and move the throw sites.
3. Update the OpenAPI contract and tests.
4. Release together with the frontend change.

Rollback: revert the change as a whole. There are no data or schema migrations.
