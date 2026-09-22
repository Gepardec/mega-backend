# Spec Delta

## Purpose

Defines one error-response contract for the whole HTTP API, hexagon and legacy endpoints alike. Clients get a problem-details body they can handle generically, and a stable `code` they switch on instead of parsing messages.

## ADDED Requirements

### Requirement: Every error response is a problem detail
Every response with a 4xx or 5xx status, from any endpoint, SHALL use media type `application/problem+json` and SHALL have a body following RFC 9457 (Problem Details for HTTP APIs). The body SHALL contain `status` (the HTTP status code as a number), `title` (the standard reason phrase of that status, for example `"Not Found"`), and `instance` (the request path). The body MAY contain `detail`, a human-readable explanation of this occurrence meant for developers, not end users. The body SHALL NOT contain `type`, so clients SHALL treat every problem's type as `about:blank`. Endpoints that are not part of the published OpenAPI contract SHALL follow the same format.

#### Scenario: Unknown resource returns a problem
- **WHEN** a client requests a month-end task that does not exist
- **THEN** the API responds with `404` and media type `application/problem+json`
- **THEN** the body contains `status: 404`, `title: "Not Found"`, and `instance` equal to the request path
- **THEN** the body contains no `type` member

#### Scenario: Legacy endpoint error returns a problem
- **WHEN** an endpoint outside the published OpenAPI contract fails with an unhandled error
- **THEN** the API responds with `500` and a problem-details body in `application/problem+json`

#### Scenario: Successful responses are unaffected
- **WHEN** a request succeeds
- **THEN** the response keeps its existing media type and body shape, and is not wrapped in a problem or envelope

### Requirement: Domain and endpoint-specific problems carry a stable code
When a bounded context rejects a request for a reason specific to it (a business rule, or an input check specific to one endpoint), the problem body SHALL contain a `code` member that identifies that reason. The code SHALL be a non-empty upper-case identifier with words separated by underscores, prefixed with the bounded context's name (for example `MONTHEND_TASK_NOT_FOUND`). Clients SHALL be able to rely on `code` to identify the reason, independent of `detail` wording, and a given reason SHALL keep its code across releases. When the shared authorization check rejects the acting user, the code SHALL be `FORBIDDEN`. Problems for any other error — authentication failures, input validation, unmatched routes, malformed request bodies, and unexpected server errors — SHALL NOT contain `code`. Clients identify those by `status` and, for input validation, by the presence of `violations`.

#### Scenario: Business-rule rejection carries the context's code
- **WHEN** a client requests a month-end clarification that does not exist
- **THEN** the API responds with `404` and `code: "MONTHEND_CLARIFICATION_NOT_FOUND"`

#### Scenario: Missing role carries the shared authorization code
- **WHEN** an authenticated user without the required role calls a role-restricted endpoint
- **THEN** the API responds with `403` and `code: "FORBIDDEN"`

#### Scenario: Unauthenticated request carries no code
- **WHEN** a request without valid authentication calls a protected endpoint
- **THEN** the API responds with `401` and a problem body without `code`

#### Scenario: Unhandled error carries no code
- **WHEN** a request fails because of an unexpected server-side error
- **THEN** the API responds with `500` and a problem body without `code`

### Requirement: Business-rule rejections keep their HTTP status
When a bounded context rejects a request, the HTTP status SHALL reflect the kind of rejection: `404` when the referenced domain object doesn't exist, `403` when the acting user is not permitted to perform the operation, and `400` when the request violates a business rule. Introducing problem details SHALL NOT change the HTTP status of any existing rejection.

#### Scenario: Non-lead toggles Leistungsnachweis
- **WHEN** an authenticated user who does not lead the project calls `PUT /projects/{projectId}/leistungsnachweis-enabled`
- **THEN** the API responds with `403` and a bounded-context-prefixed `code`

#### Scenario: Rule violation keeps 400
- **WHEN** a project lead requests bulk completion for a task type that cannot be bulk-completed by a project lead
- **THEN** the API responds with `400` and a bounded-context-prefixed `code`

### Requirement: Input validation errors list each violation
When a request is rejected because its input is invalid — a missing or malformed path parameter, query parameter, header or body field — the API SHALL respond with `400`. The problem body SHALL NOT contain `code` and SHALL contain a `violations` array with at least one entry. Each entry SHALL have:
- `field`: the name of the offending parameter or the path to the offending body property
- `in`: one of `path`, `query`, `header`, `form`, `body`, or `?` when the location can't be determined
- `message`: a human-readable explanation

This SHALL apply equally to constraint violations, to missing mandatory body fields, and to path or query values that cannot be parsed into the expected type. A request body that isn't well-formed JSON, or that has a value of the wrong JSON type, SHALL be rejected with `400` and a problem body without `code` and without `violations`; that body MAY contain a `field` member naming the offending property.

#### Scenario: Unparsable path parameter
- **WHEN** a client calls a month-end endpoint with the month path parameter `2026-13`
- **THEN** the API responds with `400` and a problem body without `code`
- **THEN** `violations` contains an entry with `field: "month"` and `in: "path"`

#### Scenario: Body value of the wrong type
- **WHEN** a client calls `PUT /projects/{projectId}/leistungsnachweis-enabled` with `enabled` set to a string that isn't a boolean
- **THEN** the API responds with `400` and a problem body without `code` and without `violations`

#### Scenario: Missing mandatory body field
- **WHEN** a client calls `PUT /projects/{projectId}/leistungsnachweis-enabled` with a body lacking `enabled`
- **THEN** the API responds with `400` and a problem body without `code`
- **THEN** `violations` contains an entry whose `field` names `enabled` and whose `in` is `body`

### Requirement: Server errors do not disclose internals
A problem with a 5xx status SHALL NOT contain exception messages, exception class names, stack traces, SQL, or personal data. It SHALL NOT contain a `detail` member. Problems with a 4xx status SHALL NOT contain stack traces or personal data such as email addresses.

#### Scenario: Unexpected failure hides the cause
- **WHEN** an endpoint fails with an unexpected exception whose message contains internal details
- **THEN** the API responds with `500` and a problem body without `code`
- **THEN** the body contains neither `detail` nor any part of the exception message

#### Scenario: Ambiguous actor resolution is a server error
- **WHEN** the authenticated user's email matches more than one user record
- **THEN** the API responds with `500` and a problem body without `code`
- **THEN** the body does not contain the email address
