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
- `quarkus-http-problem` versions follow Quarkus versions. `io.quarkus.platform:quarkus-bom` manages it from 3.38.0 on. The project is on 3.40.1, whose BOM manages extension version `3.38.2`.
- Hexagon outbound adapters call ZEP and Personio through MicroProfile REST clients, partly through the legacy `zep.rest.service` layer. A downstream HTTP error status surfaces as the client's `ClientWebApplicationException`, a `WebApplicationException` whose response carries the downstream status. Only the release-date update and `PersonioEmployeeAdapter` catch these; the ZEP reads used by work-time and month-end endpoints let them propagate.

## Goals / Non-Goals

**Goals:**
- One rendering path for all problems: the extension. Our code only classifies errors.
- Hexagon code classifies each failure once, at its definition, with no HTTP knowledge.
- The OpenAPI `Problem` schema matches the actual JSON on the wire, and a test enforces it.
- Each rejection gets the status of its kind: `400` only for contract violations, `409` for state conflicts, `422` for well-formed requests that don't fit the domain (D17).

**Non-Goals:**
- Success-side messages or response envelopes.
- Re-evaluating `404` and `403` rejections. Only the blanket `400` is split up (D17).
- Documenting legacy endpoints in the OpenAPI contract.
- Frontend changes, including the planned error interceptor.
- Adding `type` URIs or a problem-type registry.

## Decisions

### D1: `quarkus-http-problem`, version managed by the platform BOM
We add `io.quarkiverse.httpproblem:quarkus-http-problem` without a `<version>`, so the Quarkus platform BOM manages it. The original plan raised Quarkus from 3.37.1 to 3.38.3 for this; `main` has meanwhile moved to 3.40.1, so no upgrade is part of this change.
- *Alternatives:* pin an explicit extension version, which the user rejected because they want the BOM to manage it. Or write our own mappers, which would re-implement mappers for validation, auth, Jackson and catch-all, plus logging.

### D2: Shared, HTTP-free `DomainException` with `code` and `ErrorCategory`
`shared/domain/error/DomainException` (abstract, extends `RuntimeException`) exposes its `ErrorCode` through `errorCode()`; code and category are read from there. `ErrorCategory` is an enum `NOT_FOUND | FORBIDDEN | CONFLICT | UNPROCESSABLE | INVALID` (D17), and we add values only when a failure needs them. Each BC's existing abstract base (`MonthEndException`, `WorkTimeException`, `ProjectException`) extends `DomainException`. Every concrete exception passes a **constant** code and category to `super(...)`, so both are fixed per class, not per throw site. `UnknownUsersException` gets a `UserException` base like the other contexts.

The shared `ForbiddenException` (still in `shared/application/security`) also extends `DomainException`, with code `FORBIDDEN` and category `FORBIDDEN`. It currently extends `SecurityException`; nothing catches `SecurityException`, so changing the superclass is safe.

- *Why a category and not an HTTP status on the exception:* the domain states the *kind* of failure. Only the inbound adapter knows it becomes a `404`. This keeps the new ArchUnit rule (spec: hexagon-layer-constraints) satisfiable.
- *Why in the shared kernel:* the category is a stable concept shared across contexts, which is exactly what the shared-kernel requirement allows.
- *Alternative:* keep per-BC `instanceof` mapping. That's rejected because it's the duplication we're removing.

**Per-context error-code enums.** Each bounded context declares its codes in one enum in its `domain/error` package (`MonthEndErrorCode`, `ProjectErrorCode`, `WorkTimeErrorCode`, `UserErrorCode`), and each constant carries its `ErrorCategory`. The enums implement the shared `ErrorCode` interface, whose default `code()` derives the wire code as `<boundedContext>_<constant name>`, so the context prefix holds by construction. The shared `FORBIDDEN` lives in `SharedErrorCode`, which overrides `code()` to drop the prefix. `DomainException` takes an `ErrorCode`, and each context's base exception accepts only that context's enum. `ErrorCodeTest` finds every `ErrorCode` enum in the hexagon, checks that codes are unique and well-formed, and pins the full list of codes. Because a renamed constant changes the wire code, the pinned list makes such a rename fail the build until it's deliberately updated, and it gives an overview of all codes in one place.
- *Alternatives:* one global enum or constants class, rejected because every context would then depend on a shared list of all failures; explicit code strings per constant, rejected because they duplicate the constant name and leave the prefix to convention.

**One exception type per context.** Because the enum constant already names the failure and carries its category, each context has one concrete exception (`MonthEndException`, `ProjectException`, `WorkTimeException`, `UserException`) that takes its own enum constant, for example `new MonthEndException(MonthEndErrorCode.TASK_NOT_FOUND, message)`. A dedicated subclass exists only when the failure carries structured data or code must catch exactly that failure. Today that's `UnknownUsersException`, which `UserResource` catches to turn usernames into CSV line numbers. The shared `ForbiddenException` stays as it is. Tests assert on `errorCode()` instead of the exception class.
- *Alternative:* one subclass per failure. Rejected because the subclasses repeated the enum constants without adding behaviour, and nothing caught them by type.

**Code catalogue.** These are the stable values; the frontend switches on them. Statuses follow D17; the category is pinned together with the code in `ErrorCodeTest`.

| Enum constant | Category | Status | Code |
|---|---|---|---|
| `MonthEndErrorCode.TASK_NOT_FOUND` | NOT_FOUND | 404 | `MONTHEND_TASK_NOT_FOUND` |
| `MonthEndErrorCode.CLARIFICATION_NOT_FOUND` | NOT_FOUND | 404 | `MONTHEND_CLARIFICATION_NOT_FOUND` |
| `MonthEndErrorCode.ACTOR_NOT_AUTHORIZED` | FORBIDDEN | 403 | `MONTHEND_ACTOR_NOT_AUTHORIZED` |
| `MonthEndErrorCode.CLARIFICATION_CLOSED` | CONFLICT | 409 | `MONTHEND_CLARIFICATION_CLOSED` |
| `MonthEndErrorCode.EMPLOYEE_CONTEXT_NOT_FOUND` | UNPROCESSABLE | 422 | `MONTHEND_EMPLOYEE_CONTEXT_NOT_FOUND` |
| `MonthEndErrorCode.EMPLOYEE_NOT_ASSIGNED_TO_PROJECT` | UNPROCESSABLE | 422 | `MONTHEND_EMPLOYEE_NOT_ASSIGNED_TO_PROJECT` |
| `MonthEndErrorCode.PROJECT_CONTEXT_NOT_FOUND` | UNPROCESSABLE | 422 | `MONTHEND_PROJECT_CONTEXT_NOT_FOUND` |
| `MonthEndErrorCode.NO_ELIGIBLE_PROJECT_LEAD` (new, D16) | UNPROCESSABLE | 422 | `MONTHEND_NO_ELIGIBLE_PROJECT_LEAD` |
| `MonthEndErrorCode.TASK_TYPE_NOT_BULK_COMPLETABLE` (new, D16) | UNPROCESSABLE | 422 | `MONTHEND_TASK_TYPE_NOT_BULK_COMPLETABLE` |
| `ProjectErrorCode.NOT_FOUND` | NOT_FOUND | 404 | `PROJECT_NOT_FOUND` |
| `ProjectErrorCode.LEISTUNGSNACHWEIS_NOT_APPLICABLE` | CONFLICT | 409 | `PROJECT_LEISTUNGSNACHWEIS_NOT_APPLICABLE` |
| `WorkTimeErrorCode.USER_NOT_FOUND` | NOT_FOUND | 404 | `WORKTIME_USER_NOT_FOUND` |
| `UserErrorCode.INTERNAL_RATES_UNKNOWN_USERS` (`UnknownUsersException`) | UNPROCESSABLE | 422 | `USER_INTERNAL_RATES_UNKNOWN_USERS` |
| `InternalRateUploadErrorCode.INTERNAL_RATES_EMPTY_FILE` (adapter) | INVALID | 400 | `USER_INTERNAL_RATES_EMPTY_FILE` |
| `InternalRateUploadErrorCode.INTERNAL_RATES_BAD_FORMAT` (adapter) | INVALID | 400 | `USER_INTERNAL_RATES_BAD_FORMAT` |
| `SharedErrorCode.FORBIDDEN` (shared `ForbiddenException`) | FORBIDDEN | 403 | `FORBIDDEN` |

`MONTHEND_ACTOR_NOT_AUTHORIZED` also covers the clarification creator and subject rule, which `main` turned from a `400` into a `403` before this change was rebased. The shared capacity check (`requireCapacity`) rejects through the shared `ForbiddenException`, so a capacity the caller doesn't hold yields `FORBIDDEN`.

Internal-rate upload codes: `USER_INTERNAL_RATES_UNKNOWN_USERS` is `UserErrorCode.INTERNAL_RATES_UNKNOWN_USERS`, raised by the use case through `UnknownUsersException`; the resource reuses the exception's code and only adds `lines`. `USER_INTERNAL_RATES_EMPTY_FILE` and `USER_INTERNAL_RATES_BAD_FORMAT` are detected only by the CSV parsing, so they live in the adapter-local enum `InternalRateUploadErrorCode`. Every code is an `ErrorCode` enum constant where it's raised, so `ErrorCodeTest` pins all of them and no code exists only as a string. Problems produced by the framework have no code (D4).

### D3: One `DomainProblemMapper` extending the extension's `ExceptionMapperBase`
`shared/adapter/inbound/rest/DomainProblemMapper extends ExceptionMapperBase<DomainException>`, with constructor injection of `PostProcessorsRegistry`. `toProblem` starts from the public `problemOf(ErrorCode)`, which sets the status, `title` (the reason phrase) and `code`, then adds `detail = exception.getMessage()`. The category-to-status mapping lives only here: `NOT_FOUND → 404`, `FORBIDDEN → 403`, `CONFLICT → 409`, `UNPROCESSABLE → 422`, `INVALID → 400`. `422` is built by number because Jakarta REST has no constant for it. `UserResource` builds its CSV problems from `problemOf` too, so it never states a status itself. JAX-RS picks the most specific mapper, so it wins over the extension's `DefaultExceptionMapper` (`Exception`). Because it extends the base class, post-processing (logging, MDC) runs for domain problems exactly as for built-in ones.
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
`UserRepositoryAdapter.findByEmail` throws `IllegalStateException("authenticated actor resolution is ambiguous: N users share one email")`, without the email, so it becomes a `500`. `SetLeistungsnachweisEnabledService` no longer checks the project lead, because the coordinator role (#814) made the toggle coordinator-only, so it needs no actor code of its own.

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
The legacy `JacksonObjectMapperContextResolver` returns its own `new ObjectMapper()`, which skips every `ObjectMapperCustomizer`, including the one that registers the extension's `HttpProblem` serializer. With it in place, problems are rendered as plain bean dumps that contain `stackTrace`, `cause` and a nested `parameters` object. We delete the resolver and rely on the Quarkus-managed `ObjectMapper`, which already registers `JavaTimeModule` and writes dates as ISO strings. To keep the resolver's strict deserialization for our own API, `StrictServerObjectMapperResolver` (`@ConstrainedTo(SERVER)`) supplies a copy of the managed mapper with `FAIL_ON_UNKNOWN_PROPERTIES` enabled. The managed mapper itself keeps the Quarkus default (lenient), which REST clients use, so a field that ZEP or Personio adds to a response doesn't break a DTO that lacks `@JsonIgnoreProperties`.
- *Alternative (rejected):* `quarkus.jackson.fail-on-unknown-properties: true`. It also reaches every REST client, which then depends on each response DTO carrying `@JsonIgnoreProperties(ignoreUnknown = true)`.
- *Alternative (rejected):* a lenient resolver constrained to the client. Quarkus REST applies it on the server too, despite `@ConstrainedTo(CLIENT)`, which made our API lenient. A test pins both directions.
- *Alternative:* register the extension's module inside the resolver by hand. Rejected because it depends on extension internals and the resolver would keep ignoring every other Quarkus customizer.

### D12: Bean-validation violation locations
The extension resolves a body violation to `in: "body"` only when the resource method's parameter name matches the generated interface's. Otherwise it reports `in: "?"` and prefixes `field` with the parameter name. We rename the `MonthEndResource` body parameters to the generated names, so body violations resolve to `body`. The contract lists `?` as a valid `in` value, because that's the extension's actual token for an unknown location.
- *Alternatives:* a `ProblemPostProcessor` that rewrites `?` to `unknown`, which is one more hook to maintain; or dropping the unknown value from the contract, which the extension can't guarantee.

### D13: Missing body fields go through bean validation; malformed bodies keep the extension's shape
With `jaxrs-spec`'s default `generateJsonCreator=true`, request DTOs get a `@JsonCreator` with `@JsonProperty(required = true)`. A missing required field then fails inside Jackson before bean validation runs, and the extension's `MismatchedInputExceptionMapper` answers with `detail: "Malformed request body"` and a top-level `field` (often `?`) in place of `violations`. We set `generateJsonCreator=false`: a missing field deserializes to `null`, and the generated `@NotNull` reports it as a `violations` entry with `in: "body"`. Malformed JSON and values of the wrong JSON type still reach the extension's Jackson mappers. We accept their shape (`400`, no `code`, no `violations`, optional `field`), and the `Problem` schema declares `field` as optional.
- *Alternatives:* a `ProblemPostProcessor` that turns the Jackson `field` problems into `violations`, which adds a hook for a case clients can't fix at runtime anyway; or keeping the creators and post-processing everything, where missing fields would only get `field: "?"`.

### D14: Failed outbound REST-client calls become `502` without details
`shared/adapter/inbound/rest/OutboundHttpFailureProblemMapper extends ExceptionMapperBase<ClientWebApplicationException>` answers `502 Bad Gateway` with no `detail` and no `code`, whatever status the external system returned. It's more specific than the extension's `WebApplicationExceptionMapper`, so JAX-RS picks it, and extending `ExceptionMapperBase` keeps the extension's ERROR log line with the stack trace, which still names the failing client method for operators. Connection failures and timeouts don't carry a response, so they still reach the catch-all `500`.
- *Why:* without it the extension's `WebApplicationExceptionMapper` copies the downstream status and the client's message ("…when invoking: Rest Client method: '…'") into our problem. A ZEP `401` would make the frontend treat the user as logged out, and a ZEP `404` would look like a missing resource of ours.
- *Why `502` and not `500`:* the fault is outside this service, and clients and operators can tell "external system failed" apart from our own bugs by status alone. Like other generic problems it carries no `code` (D4).
- *Alternative (rejected for now):* make every outbound adapter translate client exceptions into a port-level failure. It's the purer hexagonal shape, but it touches about ten adapters and the legacy `zep.rest.service` layer, and every adapter added later would have to remember it; the inbound mapper is a safety net that holds by construction. Adapters that need a different outcome (the release-date update collects per-entry failures) keep catching locally.

### D15: Resource parameter names match the generated interfaces
D12 applies to every hexagon resource, not only `MonthEndResource`. A unit test reflects over every class implementing a generated `..hexagon.generated.api..` interface and checks that each implementing method uses the interface's parameter names. This catches resources added later, such as `WorkTimeWarningRuleResource`, whose body parameter `update` made violations report `in: "?"` and `field: "update.enabled"`. The build compiles with `-parameters`, so the names are available at runtime.

### D16: Structural invariants are defects, not caller errors
The compact constructors of `MonthEndTask` and `MonthEndClarification` throw `IllegalArgumentException` instead of a `MonthEndException`, so a violation becomes a `500` without `code` or `detail`. These constructors also run when MapStruct rebuilds an aggregate from the database, so a corrupt row used to reach the caller as a `400` with the invariant text in `detail`. The caller can't fix that, the frontend would call the request invalid, and no alert fires. The missing-ZEP-username check in the work-time services throws `IllegalStateException` for the same reason. `MONTHEND_VALIDATION_FAILED` and `WORKTIME_VALIDATION_FAILED` are removed.

Rules a request can break are checked before construction and get their own code (statuses per D17):
- Bulk completion of a task type a project lead can't bulk-complete: `MONTHEND_TASK_TYPE_NOT_BULK_COMPLETABLE`, thrown by the use case. The type comes from the request body and the contract allows every task type.
- A clarification for a project without an active lead this month: `MONTHEND_NO_ELIGIBLE_PROJECT_LEAD`, thrown by the creation factory next to the creator check. It's a business state, not a bug.
- Blank clarification text: `@NotBlank` on `text` in `CreateClarificationRequest` and `UpdateClarificationTextRequest`, the same as premature preparation, so it becomes a bean-validation `400` with a `violations` entry. The constructor check remains as a guard for the ZEP mail path, whose catch-all reports failures by mail.
- *Rule of thumb:* if a different request could have succeeded, the failure is a domain exception with a code; if not, it's an unchecked JDK exception and a `500`. A guard that fires on the creation path means an earlier check is missing, which is also a defect.

### D17: Status by kind of rejection
RFC 9110 gives each 4xx a meaning, and the blanket `400` hid that. The rule, as categories:
- `NOT_FOUND` → `404`: the object the request **URL** addresses doesn't exist. Objects named only in the body never give `404`, because the target URL (for example `/monthend/clarifications`) exists.
- `FORBIDDEN` → `403`: the acting user may not perform the operation.
- `CONFLICT` → `409`: the current state of the object the URL addresses doesn't allow the operation. `MONTHEND_CLARIFICATION_CLOSED` (edit or delete a done clarification) and `PROJECT_LEISTUNGSNACHWEIS_NOT_APPLICABLE` (the addressed project isn't billable).
- `UNPROCESSABLE` → `422`: the request satisfies the contract, but what it refers to or asks for doesn't fit the domain. The month-end context codes, `MONTHEND_NO_ELIGIBLE_PROJECT_LEAD`, `MONTHEND_TASK_TYPE_NOT_BULK_COMPLETABLE` and `USER_INTERNAL_RATES_UNKNOWN_USERS`.
- `INVALID` → `400`: the content is malformed. Only the CSV's empty-file and bad-format codes; bean validation and parse errors are `400` from the framework.
- *Frontend:* the failure mapper turns every 4xx other than 401/403/404 into the same reason and special-cases these codes by `code`, so nothing changes there. The status split is for HTTP semantics, monitoring and any other client, not for the current UI.
- *Alternative (rejected):* `404` for references in the body. It confuses "this URL doesn't exist" with "a value in your request doesn't fit".
- *Alternative (rejected):* keep `400` for everything and leave the distinction to `code`. That's what the first draft did; it makes the status carry no information beyond "client error".

### D10: Tests
- A REST-Assured contract test class in `shared/adapter/inbound/rest` covering:
  - a domain `404` (status, media type, `title`, `instance`, `code`, `detail`, no `type`);
  - a malformed path value `400` without `code` or `violations`;
  - a bean-validation `400`;
  - a `401` without `code`, and a `403` (role check) with `code: FORBIDDEN`;
  - a `500` without `detail` or `code`;
  - a failed outbound REST-client call (`404` from the external system) answered as `502` without `detail` or `code` (D14);
  - success responses still using `application/json`.
- Deserialize problems into the generated `ProblemDto`, so drift between schema and wire fails the test. REST-Assured's mapper rejects unknown members, so an unexpected extra member on a generic problem fails the test too.
- Existing REST tests that read `ApiErrorDto` (`MonthEndResourceTest`, `WorkTimeResourceTest`) and `errorCode` (`UserResourceTest`) assert on `code` instead.
- `HexagonalArchitectureTest` gets the JAX-RS / problem-library rule for `..domain..` and `..application..`.
- A parameter-name test enforces D15 for every resource that implements a generated API interface.

## Risks / Trade-offs

- [`ExceptionMapperBase` is internal API and could change in an extension release] → The contract test in D10 covers domain problems end to end, so a breaking upgrade fails the build. If that happens, switch to the post-processor approach (D3 alternative), which needs no contract change.
- [Extension mappers take priority over custom ones for the same exception type] → We don't register any mapper for a built-in type. `DomainProblemMapper` targets our own type only.
- [Frontend breaks on every error-handling path at once] → This is accepted and the change is marked breaking. Deploy together with the frontend release that reads `code`. There's no transition mode.
- [Legacy endpoints change error bodies without a contract] → They were empty or ad hoc before. The frontend's future interceptor reads `code` from any `application/problem+json` response.
- [Domain messages end up in `detail` and might contain identifiers] → Current messages hold IDs and field names, not personal data. The `UserRepositoryAdapter` message that contained an email becomes a 500, which never gets `detail`. New domain messages must not include personal data.
- [Reclassifying invariant failures as `500` (D16) raises the 5xx rate if stored data is already inconsistent] → Intended: those rows were hidden behind `400`s nobody alerted on. A `500` logs at ERROR with the stack trace that names the broken rule.
- [A `WebApplicationException` carrying a `Response` entity bypasses every mapper (per the extension's TROUBLESHOOTING.md)] → We throw none ourselves. REST-client exceptions that reach the inbound side are handled by D14.
- [The mapper in D14 depends on the REST client's exception type] → It's the documented type of the Quarkus REST client, and a REST test throws it end to end, so a change fails the build.

## Migration Plan

1. Add the extension, the shared error types and `DomainProblemMapper`, then remove the old mappers and move the throw sites.
2. Update the OpenAPI contract and tests.
3. Release together with the frontend change.

Rollback: revert the change as a whole. There are no data or schema migrations.
