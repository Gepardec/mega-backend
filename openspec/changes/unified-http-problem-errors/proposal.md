# Proposal

## Why

Error responses are inconsistent across the API, and clients can't reliably tell errors apart. Hexagon endpoints return `{ "message": "..." }` holding an English developer string. Legacy mappers return empty bodies for 401 and 500 and a different list shape for validation errors. The internal-rate CSV upload uses a third shape (`errorCode` + `lines`). The new frontend uses every endpoint, legacy included. Without a machine-readable identifier it can only switch on HTTP status, which is why its error handling is inconsistent too. The hexagon also maps its own errors with three near-identical per-BC exception mappers that choose the HTTP status through `instanceof` chains.

## What Changes

- **BREAKING**: Every error response from every endpoint, hexagon and legacy, becomes an RFC 9457 problem detail with media type `application/problem+json`. The body carries `title`, `status`, `instance`, and an optional `detail`. Problems raised for a domain or endpoint-specific reason also carry a stable `code` extension member that clients switch on. `type` is not used.
- **BREAKING**: The `ApiError` schema (`{ message }`) is removed from the OpenAPI contract and replaced by a `Problem` schema.
- **BREAKING**: Bean-validation errors (missing mandatory fields and constraint violations) share one shape: a `400` problem with a `violations` array of `{ field, in, message }`. Path or query values that can't be parsed into their type, such as months and identifiers, get the framework's default 4xx problem without `violations`.
- **BREAKING**: Internal-rate CSV upload failures become problems. The existing error codes move to `code`, and `lines` stays as an extension member. The `errorCode` field is removed.
- **BREAKING**: The ZEP mail webhook no longer echoes the raw exception message in its 500 response. It returns a problem like every other unhandled error.
- Problems produced by the framework (authentication, input validation, unknown routes, malformed JSON, unexpected errors) carry no `code`. Clients identify them by `status` and, for validation, by `violations`.
- 5xx responses never expose exception messages.
- **BREAKING**: When a call to an external system (ZEP, Personio) fails with an HTTP error status, the API answers `502` without `code` or `detail`. Until now the downstream status and the REST client's message were passed through, so a ZEP `401` or `404` looked like our own `401` or `404`.
- The Quarkiverse `quarkus-http-problem` extension renders and logs all problems. Its version is managed by the Quarkus platform BOM, which manages it from Quarkus 3.38 on; the project is already on a newer Quarkus, so no upgrade is part of this change.
- Hexagon errors are classified in one place. A shared, HTTP-free domain error base carries a stable `code` and a category. A single inbound mapper turns the category into an HTTP status. The per-BC domain mappers, the per-BC REST-adapter exception hierarchies and their mappers, and the hexagon forbidden mapper are removed.
- Hexagon inbound adapters no longer parse input by hand. Months are typed as year-month in the contract and generated as a native year-month type. The transport helpers are removed, and resources wrap identifiers into domain types directly.
- All six legacy exception mappers are removed, together with the legacy `UnauthorizedException`, which nothing throws.
- A duplicate-email conflict when resolving the authenticated actor is now a 500 instead of a 403, because it is a data-integrity fault, not an authorization decision.
- The HTTP status of every existing business-rule rejection stays the same.
- **BREAKING**: Failures that were never caller errors stop being reported as `400`. Stored month-end data that breaks a structural rule and a user without a ZEP username now give `500` without `code` or `detail`. The catch-all codes `MONTHEND_VALIDATION_FAILED` and `WORKTIME_VALIDATION_FAILED` are removed. The two rules a request can break that they also covered get their own codes, `MONTHEND_TASK_TYPE_NOT_BULK_COMPLETABLE` and `MONTHEND_NO_ELIGIBLE_PROJECT_LEAD`, and blank clarification text is rejected by input validation.

## Capabilities

### New Capabilities
- `http-error-responses`: A cross-cutting contract for error responses across the whole API: problem-detail format and media type, the `code` member for domain and endpoint-specific errors, the validation-error shape, non-disclosure of internal details in 5xx responses, and how failures of external systems surface.

### Modified Capabilities
- `user-rest-api`: The internal-rate CSV upload's failure body moves from `{ errorCode, lines }` to a problem with `code` and `lines`.
- `monthend-zep-mail-processing`: The webhook's 500 response no longer contains the raw exception message.
- `hexagon-boundary-conventions`: Adds how hexagon failures are expressed: domain and application failures carry a stable code and an HTTP-free category, and only inbound adapters translate them into HTTP; inbound adapters take input already typed by the contract instead of parsing it.
- `hexagon-layer-constraints`: Adds an architecture rule: hexagon domain and application packages must not depend on JAX-RS or the problem-details library.

## Impact

- **API contract**: `openapi/schemas/shared.yaml` (`ApiError` becomes `Problem`, plus a violation item schema), `openapi/responses/common.yaml` (all error responses switch to `application/problem+json`), and `openapi/paths/user.yaml` together with `openapi/schemas/user.yaml` (the CSV upload error schema). Month fields and parameters use `format: year-month` instead of a pattern-constrained string. The frontend's generated client changes for every error type.
- **Frontend**: Has to read `code`, not `message` or `errorCode`, and handle `application/problem+json`. Legacy endpoints, which aren't in the bundled contract, change their error bodies at runtime too.
- **Dependencies**: The new `io.quarkiverse.httpproblem:quarkus-http-problem`, version managed by the Quarkus platform BOM.
- **Hexagon code**: Error types in `monthend`, `worktime`, `project` and `user` domains, the shared `ForbiddenException`, inbound adapter transport helpers (removed), `UserResource` (CSV upload), `ZepMailWebhookResource`, `SetLeistungsnachweisEnabledService`, `UserRepositoryAdapter`, a shared inbound mapper for failed outbound REST-client calls, the body parameter names of hexagon resources, and `HexagonalArchitectureTest`.
- **Legacy code**: The `application/exception/mapper` package, `application/exception/UnauthorizedException`, `domain/model/ValidationViolation`, and their tests.
- **Tests**: REST tests that deserialize `ApiErrorDto` or assert on `errorCode` move to problem assertions. The legacy mapper unit tests are removed.
- **Logging**: The extension logs client errors at INFO and server errors at ERROR. The hexagon forbidden mapper's WARN line, which logged the user's email, goes away.
