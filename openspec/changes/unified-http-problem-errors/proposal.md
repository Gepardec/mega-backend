# Proposal

## Why

Error responses are inconsistent across the API, and clients can't reliably tell errors apart. Hexagon endpoints return `{ "message": "..." }` holding an English developer string. Legacy mappers return empty bodies for 401 and 500 and a different list shape for validation errors. The internal-rate CSV upload uses a third shape (`errorCode` + `lines`). The new frontend uses every endpoint, legacy included. Without a machine-readable identifier it can only switch on HTTP status, which is why its error handling is inconsistent too. The hexagon also maps its own errors with three near-identical per-BC exception mappers that choose the HTTP status through `instanceof` chains.

## What Changes

- **BREAKING**: Every error response from every endpoint, hexagon and legacy, becomes an RFC 9457 problem detail with media type `application/problem+json`. The body carries `title`, `status`, `instance`, and an optional `detail`. Problems raised for a domain or endpoint-specific reason also carry a stable `code` extension member that clients switch on. `type` is not used.
- **BREAKING**: The `ApiError` schema (`{ message }`) is removed from the OpenAPI contract and replaced by a `Problem` schema.
- **BREAKING**: Input validation errors (bean validation, and unparsable path, query or body values in hexagon adapters) share one shape: a `400` problem with a `violations` array of `{ field, in, message }`.
- **BREAKING**: Internal-rate CSV upload failures become problems. The existing error codes move to `code`, and `lines` stays as an extension member. The `errorCode` field is removed.
- **BREAKING**: The ZEP mail webhook no longer echoes the raw exception message in its 500 response. It returns a problem like every other unhandled error.
- Problems produced by the framework (authentication, input validation, unknown routes, malformed JSON, unexpected errors) carry no `code`. Clients identify them by `status` and, for validation, by `violations`.
- 5xx responses never expose exception messages.
- The Quarkiverse `quarkus-http-problem` extension renders and logs all problems. Quarkus moves from 3.37.1 to 3.38.x so the Quarkus platform BOM manages the extension's version.
- Hexagon errors are classified in one place. A shared, HTTP-free domain error base carries a stable `code` and a category. A single inbound mapper turns the category into an HTTP status. The per-BC domain mappers, the per-BC REST-adapter exception hierarchies and their mappers, and the hexagon forbidden mapper are removed.
- All six legacy exception mappers are removed, together with the legacy `UnauthorizedException`, which nothing throws.
- A duplicate-email conflict when resolving the authenticated actor is now a 500 instead of a 403, because it is a data-integrity fault, not an authorization decision.
- The HTTP status of every existing domain error stays the same.

## Capabilities

### New Capabilities
- `http-error-responses`: A cross-cutting contract for error responses across the whole API: problem-detail format and media type, the `code` member for domain and endpoint-specific errors, the validation-error shape, and non-disclosure of internal details in 5xx responses.

### Modified Capabilities
- `user-rest-api`: The internal-rate CSV upload's failure body moves from `{ errorCode, lines }` to a problem with `code` and `lines`.
- `monthend-zep-mail-processing`: The webhook's 500 response no longer contains the raw exception message.
- `hexagon-boundary-conventions`: Adds how hexagon failures are expressed: domain and application failures carry a stable code and an HTTP-free category, and only inbound adapters translate them into HTTP.
- `hexagon-layer-constraints`: Adds an architecture rule: hexagon domain and application packages must not depend on JAX-RS or the problem-details library.

## Impact

- **API contract**: `openapi/schemas/shared.yaml` (`ApiError` becomes `Problem`, plus a violation item schema), `openapi/responses/common.yaml` (all error responses switch to `application/problem+json`), and `openapi/paths/user.yaml` together with `openapi/schemas/user.yaml` (the CSV upload error schema). The frontend's generated client changes for every error type.
- **Frontend**: Has to read `code`, not `message` or `errorCode`, and handle `application/problem+json`. Legacy endpoints, which aren't in the bundled contract, change their error bodies at runtime too.
- **Dependencies**: Quarkus platform upgrade 3.37.1 to 3.38.x, and the new `io.quarkiverse.httpproblem:quarkus-http-problem`.
- **Hexagon code**: Error types in `monthend`, `worktime`, `project` and `user` domains, the shared `ForbiddenException`, inbound adapter transport helpers, `UserResource` (CSV upload), `ZepMailWebhookResource`, `SetLeistungsnachweisEnabledService`, `UserRepositoryAdapter`, and `HexagonalArchitectureTest`.
- **Legacy code**: The `application/exception/mapper` package, `application/exception/UnauthorizedException`, `domain/model/ValidationViolation`, and their tests.
- **Tests**: REST tests that deserialize `ApiErrorDto` or assert on `errorCode` move to problem assertions. The legacy mapper unit tests are removed.
- **Logging**: The extension logs client errors at INFO and server errors at ERROR. The hexagon forbidden mapper's WARN line, which logged the user's email, goes away.
