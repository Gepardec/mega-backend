# Spec Delta

## ADDED Requirements

### Requirement: Hexagon failures carry a stable code and a transport-neutral category
Every failure that hexagon domain or application code raises to reject a request SHALL carry a stable error code and a category. The category SHALL come from a shared, transport-neutral vocabulary: not found, forbidden, or invalid. The code and category SHALL be fixed per kind of failure, not per throw site, and the code SHALL be prefixed with the owning bounded context (for example `MONTHEND_TASK_NOT_FOUND`). Only inbound adapters SHALL decide how a category is expressed in a transport protocol such as HTTP. Domain and application code SHALL NOT choose status codes or build transport responses. Failures caused by inconsistent data or broken invariants that the caller can't fix SHALL NOT be classified as a caller error; they SHALL surface as unexpected server errors.

#### Scenario: New domain failure declares its code and category
- **WHEN** a hexagon bounded context introduces a new failure that rejects a request
- **THEN** that failure declares a context-prefixed code and one of the shared categories, without referring to HTTP

#### Scenario: Inbound adapter translates the category
- **WHEN** a hexagon failure with category "not found" reaches the HTTP boundary
- **THEN** the HTTP inbound adapter responds with `404` and the failure's code, with no bounded-context-specific mapping code

#### Scenario: Authorization failure outside a bounded context uses the shared category
- **WHEN** the shared authenticated-actor or role check rejects a request
- **THEN** it raises a failure with the "forbidden" category and the API responds with `403` and `code: "FORBIDDEN"`

#### Scenario: Data inconsistency is not reported as a caller error
- **WHEN** resolving the authenticated actor finds more than one user with the same email
- **THEN** the failure surfaces as an unexpected server error, not as "forbidden"

### Requirement: Inbound adapters leave input parsing to the contract
Hexagon inbound adapters SHALL receive path, query and body values already converted to the types declared in the API contract, such as months and identifiers, and SHALL NOT parse or re-validate them by hand. Values that can't be converted SHALL be rejected before the adapter runs, using the API-wide problem format. Adapters SHALL NOT define bounded-context-specific adapter exception hierarchies.

#### Scenario: Unparsable month is rejected before the adapter
- **WHEN** a hexagon REST endpoint receives a month path parameter that isn't a valid year-month
- **THEN** the request is rejected with a problem response without `code`, and no use case is invoked
