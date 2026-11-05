# Spec Delta

## Purpose

Defines one error-response contract for the HTTP API. It covers every hexagon endpoint, and legacy endpoints for the errors they let propagate; legacy resources that build their own error responses are out of scope. Clients get a problem-details body they can handle generically, and a stable `code` they switch on instead of parsing messages.

## ADDED Requirements

### Requirement: Every error response is a problem detail
Every response with a 4xx or 5xx status from a hexagon endpoint SHALL use media type `application/problem+json` and SHALL have a body following RFC 9457 (Problem Details for HTTP APIs). The body SHALL contain `status` (the HTTP status code as a number), `title` (the standard reason phrase of that status, for example `"Not Found"`), and `instance` (the request path). The body MAY contain `detail`, a human-readable explanation of this occurrence meant for developers, not end users. The body SHALL NOT contain `type`, so clients SHALL treat every problem's type as `about:blank`. Legacy endpoints, which are not part of the published OpenAPI contract, SHALL follow the same format for every error that propagates out of the resource. Legacy resources that catch an error and build their own error response are not covered.

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

### Requirement: Business-rule rejections use the status of their kind
When a bounded context rejects a request, the HTTP status SHALL reflect the kind of rejection: `404` when the object the request URL addresses doesn't exist, `403` when the acting user may not perform the operation, `409` when the current state of the addressed object doesn't allow the operation, and `422` when the request is well-formed but what it refers to or asks for doesn't fit the domain. `400` SHALL be reserved for requests that don't satisfy the contract or whose content is malformed.

#### Scenario: Bulk completion by an ineligible actor
- **WHEN** an authenticated user requests bulk completion of a task type on a project where they are not an eligible actor for that type
- **THEN** the API responds with `403` and the code `MONTHEND_ACTOR_NOT_AUTHORIZED`

#### Scenario: Editing a closed clarification
- **WHEN** the creator edits the text of, or deletes, a clarification that is already done
- **THEN** the API responds with `409` and the code `MONTHEND_CLARIFICATION_CLOSED`

#### Scenario: Unsupported bulk completion type
- **WHEN** a project lead requests bulk completion for a task type that cannot be bulk-completed by a project lead
- **THEN** the API responds with `422` and the code `MONTHEND_TASK_TYPE_NOT_BULK_COMPLETABLE`

#### Scenario: Clarification about an employee outside the project
- **WHEN** an authenticated user creates a clarification about an employee who isn't assigned to the project in that month
- **THEN** the API responds with `422` and the code `MONTHEND_EMPLOYEE_NOT_ASSIGNED_TO_PROJECT`

#### Scenario: Clarification for a project without an active lead
- **WHEN** an authenticated user creates a clarification, directly or through premature month-end preparation, for a project that has no active project lead in that month
- **THEN** the API responds with `422` and the code `MONTHEND_NO_ELIGIBLE_PROJECT_LEAD`

### Requirement: Faults that the caller cannot fix are server errors
A failure that no different request could have avoided SHALL NOT be reported as a `4xx`. It SHALL surface as `500` without `code` or `detail`, even where it was reported as `400` before. This covers stored data that breaks a structural rule of a domain object and a user without the ZEP username that a work-time read needs.

#### Scenario: Stored data that breaks a structural rule
- **WHEN** a request loads a month-end task or clarification whose stored state breaks a structural rule, for example a completed task without a completing actor
- **THEN** the API responds with `500` and a problem body without `code` and without `detail`

#### Scenario: User without ZEP username
- **WHEN** a work-time read is requested for a user who has no ZEP username
- **THEN** the API responds with `500` and a problem body without `code` and without `detail`

### Requirement: Input validation errors list each violation
When a request is rejected because it violates a constraint declared in the contract — a missing mandatory parameter or body field, or a value outside its declared constraints — the API SHALL respond with `400`. The problem body SHALL NOT contain `code` and SHALL contain a `violations` array with at least one entry. Each entry SHALL have:
- `field`: the name of the offending parameter or the path to the offending body property
- `in`: one of `path`, `query`, `header`, `form`, `body`, or `?` when the location can't be determined
- `message`: a human-readable explanation

A path or query value that can't be parsed into its declared type (for example a month that isn't in `yyyy-MM` format, or an identifier that isn't a UUID) SHALL be rejected with a 4xx problem without `code` and without `violations`; the exact status is the framework's default. A request body that isn't well-formed JSON, or that has a value that can't be read as its declared type, SHALL be rejected with `400` and a problem body without `code` and without `violations`; that body MAY contain a `field` member naming the offending property.

#### Scenario: Unparsable path parameter
- **WHEN** a client calls a month-end endpoint with the month path parameter `2026-13`
- **THEN** the API responds with a 4xx status and a problem body without `code` and without `violations`

#### Scenario: Body value of the wrong type
- **WHEN** a client calls `PUT /projects/{projectId}/leistungsnachweis-enabled` with `enabled` set to a string that isn't a boolean
- **THEN** the API responds with `400` and a problem body without `code` and without `violations`

#### Scenario: Missing mandatory body field
- **WHEN** a client calls `PUT /projects/{projectId}/leistungsnachweis-enabled` with a body lacking `enabled`
- **THEN** the API responds with `400` and a problem body without `code`
- **THEN** `violations` contains an entry whose `field` names `enabled` and whose `in` is `body`

#### Scenario: Blank clarification text
- **WHEN** a client creates a clarification or edits its text with a value that is empty or contains only whitespace
- **THEN** the API responds with `400`, a problem body without `code`, and a `violations` entry with `field: "text"` and `in: "body"`

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

### Requirement: Failures of external systems do not leak through
When an external system the API calls, such as ZEP, answers with an HTTP error status, the API SHALL respond with `502` and a problem body without `code` and without `detail`, whatever status the external system returned. Failing to reach the external system at all, for example on a timeout, SHALL surface as an unexpected server error.

#### Scenario: External system rejects the call
- **WHEN** a client requests its work time report and ZEP answers the API's call with `404`
- **THEN** the API responds with `502` and a problem body in `application/problem+json`
- **THEN** the body contains neither `code` nor `detail`

#### Scenario: External system authentication failure is not the caller's
- **WHEN** ZEP answers a call the API makes on behalf of an authenticated user with `401`
- **THEN** the API responds with `502`, not `401`
